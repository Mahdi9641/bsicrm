package com.bsicenter.crm.service;

import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessManagementService {
  private final Db db; private final Access a; private final PasswordEncoder encoder;
  public AccessManagementService(Db db,Access a,PasswordEncoder encoder){this.db=db;this.a=a;this.encoder=encoder;}
  public record RoleWrite(@NotBlank @Size(max=120) String name,@NotBlank String baseRole,@Positive long salonId,
      boolean active,@NotNull @Size(max=100) Set<String> permissions,@NotNull @Min(0) Long version) {}
  public record UserWrite(@NotBlank @Size(max=160) String name,@NotBlank @Size(max=80) String username,
      @Size(min=12,max=72) String password,@Positive long roleId,@Positive long branchId,Long staffId,Long customerId,
      boolean active,@NotNull @Size(max=100) Map<String,Boolean> overrides,@NotNull @Min(0) Long version) {}
  public record Password(@NotBlank @Size(min=12,max=72) String password) {}
  private boolean admin(){return a.role().equals("ADMIN");}
  private void manager(){a.roles("ADMIN","MANAGER");a.requireSubscription();}
  private void lock(){db.jdbc.execute("select pg_advisory_xact_lock(741004)");}
  private void salon(long id){if(!admin()&&id!=a.salon())throw ApiError.missing();db.one("select id from salons where id=?",id);}
  private Map<String,Object> branch(long id){var b=db.one("select * from branches where id=? and active",id);salon(Db.number(b,"salon_id"));return b;}
  private void password(String value){if(value==null||value.length()<12||value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw ApiError.bad("رمز باید حداقل ۱۲ نویسه و حداکثر ۷۲ بایت باشد");}
  private void version(long actual,Long requested){if(requested==null||actual!=requested)throw ApiError.conflict("اطلاعات دسترسی تغییر کرده است؛ دوباره دریافت کنید");}
  private void assignable(String base){
    if(!PermissionCatalog.BASE_ROLES.contains(base))throw ApiError.bad("نوع حساب معتبر نیست");
    if(!admin()&&Set.of("ADMIN","MANAGER").contains(base))throw ApiError.bad("تنظیم حساب مدیریتی فقط توسط مدیر سامانه مجاز است");
  }
  private void validatePermissions(String base,Collection<String> permissions){
    var ceiling=PermissionCatalog.ceiling(base);
    for(String p:permissions)if(!PermissionCatalog.known(p)||!ceiling.contains(p)||(!admin()&&!base.equals("CUSTOMER")&&!a.has(p)))throw ApiError.bad("مجوز انتخاب‌شده خارج از دامنهٔ دسترسی شما یا نوع حساب است");
    for(String p:permissions)if(p.endsWith(".write")&&!permissions.contains(p.substring(0,p.length()-6)+".read"))throw ApiError.bad("مجوز ثبت و تغییر به مجوز مشاهدهٔ همان بخش نیاز دارد");
  }
  private List<Map<String,Object>> roleRows(){return db.list("select r.*,(select count(*) from app_users u where u.access_role_id=r.id and (? or u.salon_id=?)) assigned_count from access_roles r where r.built_in or (? or r.salon_id=?) order by r.built_in desc,r.id",admin(),a.salon(),admin(),a.salon());}
  private Map<String,Object> roleView(Map<String,Object> role){
    var out=new LinkedHashMap<>(role);out.put("permissions",rolePermissions(Db.number(role,"id")));return out;
  }
  private Set<String> rolePermissions(long id){var result=new TreeSet<String>();for(var p:db.list("select permission from access_role_permissions where role_id=?",id))result.add(p.get("permission").toString());return result;}
  public Object catalog(){
    manager();a.anyPermission("roles.read","users.read");
    var ceilings=new LinkedHashMap<String,Object>();
    for(String base:PermissionCatalog.BASE_ROLES){if(!admin()&&Set.of("ADMIN","MANAGER").contains(base))continue;var set=new TreeSet<>(PermissionCatalog.ceiling(base));if(!admin()&&!base.equals("CUSTOMER"))set.retainAll(a.policy().permissions());ceilings.put(base,set);}
    return Map.of("permissions",PermissionCatalog.definitions(),"ceilings",ceilings,"mandatory",List.of("security.read","security.write"));
  }
  public Object roles(){manager();a.anyPermission("roles.read","users.read");return roleRows().stream().map(this::roleView).toList();}
  @Transactional public Object saveRole(Long id,RoleWrite body){
    lock();manager();a.permission("roles.write");salon(body.salonId());assignable(body.baseRole());
    if(body.baseRole().equals("ADMIN"))throw ApiError.bad("نقش مدیر سامانه ثابت و غیرقابل تکثیر است");
    validatePermissions(body.baseRole(),body.permissions());
    Set<String> permissions=PermissionCatalog.normalize(body.permissions());
    if(id!=null){
      var row=db.one("select * from access_roles where id=? for update",id);salonRole(row);
      if(Boolean.TRUE.equals(row.get("built_in")))throw ApiError.bad("نقش‌های پایه قابل تغییر نیستند؛ یک نسخهٔ سفارشی بسازید");
      version(Db.number(row,"version"),body.version());
      if(Db.number(row,"salon_id")!=body.salonId()||!row.get("base_role").equals(body.baseRole()))throw ApiError.bad("سالن و نوع حساب نقش موجود قابل تغییر نیست");
      if(!admin()&&Db.number(a.user(),"access_role_id")==id)throw ApiError.bad("نمی‌توانید دسترسی نقش جاری خود را تغییر دهید");
      db.run("update access_roles set name=?,active=?,version=version+1 where id=?",body.name().trim(),body.active(),id);
      db.run("delete from access_role_permissions where role_id=?",id);
    }else id=db.id("insert into access_roles(name,base_role,salon_id,active) values(?,?,?,?) returning id",body.name().trim(),body.baseRole(),body.salonId(),body.active());
    for(String p:permissions)db.run("insert into access_role_permissions(role_id,permission) values(?,?)",id,p);
    db.run("update app_users set access_version=access_version+1 where access_role_id=?",id);
    db.run("update auth_sessions set revoked=true where user_id in(select id from app_users where access_role_id=?)",id);
    a.audit("ROLE_UPDATED","ACCESS_ROLE",id,"مجوزها="+String.join("،",permissions));
    return roleView(db.one("select * from access_roles where id=?",id));
  }
  private void salonRole(Map<String,Object> role){if(role.get("salon_id")!=null)salon(Db.number(role,"salon_id"));}
  private Map<String,Object> target(long id,boolean write){
    manager();a.permission("users."+(write?"write":"read"));
    var u=db.one("select u.*,b.name branch_name from app_users u join branches b on b.id=u.branch_id where u.id=?"+(write?" for update of u":""),id);
    if(!admin()&&(Db.number(u,"salon_id")!=a.salon()||Set.of("ADMIN","MANAGER").contains(u.get("role"))))throw ApiError.missing();
    return u;
  }
  private Map<String,Object> userView(Map<String,Object> u){
    var out=new LinkedHashMap<String,Object>();for(String k:List.of("id","name","username","role","branch_id","salon_id","staff_id","customer_id","active","access_role_id","access_version","locked_until","branch_name"))out.put(k,u.get(k));
    var snapshot=a.policyFor(u);out.put("permissions",snapshot.permissions());out.put("menus",snapshot.menus());out.put("role_name",snapshot.roleName());
    var overrides=new TreeMap<String,Boolean>();for(var row:db.list("select permission,allowed from user_permission_overrides where user_id=?",u.get("id")))overrides.put(row.get("permission").toString(),Boolean.TRUE.equals(row.get("allowed")));out.put("overrides",overrides);return out;
  }
  public Object users(){
    manager();a.permission("users.read");
    return db.list("select u.*,b.name branch_name from app_users u join branches b on b.id=u.branch_id where (? or (u.salon_id=? and u.role not in ('ADMIN','MANAGER'))) order by u.id",admin(),a.salon()).stream().map(this::userView).toList();
  }
  public Object user(long id){return userView(target(id,false));}
  public Object options(Long branchId){
    manager();a.anyPermission("users.read","roles.read");
    var branches=db.list("select b.id,b.name,b.salon_id,b.active,s.name salon_name from branches b join salons s on s.id=b.salon_id where (? or b.salon_id=?) order by b.id",admin(),a.salon());
    var salons=db.list("select id,name from salons where (? or id=?) order by id",admin(),a.salon());
    List<Map<String,Object>> staff=List.of(),customers=List.of();
    if(branchId!=null){a.permission("users.read");branch(branchId);staff=db.list("select id,name from staff where branch_id=? and active",branchId);customers=db.list("select c.id,c.first_name||' '||c.last_name name,c.mobile,(select u.id from app_users u where u.customer_id=c.id) user_id from customers c where c.branch_id=? and c.active order by c.id",branchId);}
    return Map.of("branches",branches,"salons",salons,"staff",staff,"customers",customers);
  }
  @Transactional public Object saveUser(Long id,UserWrite body){
    lock();manager();a.permission("users.write");
    var old=id==null?null:target(id,true);
    var branch=branch(body.branchId());long salon=Db.number(branch,"salon_id");
    var role=db.one("select * from access_roles where id=? and active",body.roleId());salonRole(role);
    String base=role.get("base_role").toString();assignable(base);
    if(role.get("salon_id")!=null&&Db.number(role,"salon_id")!=salon)throw ApiError.bad("نقش انتخاب‌شده متعلق به سالن کاربر نیست");
    if(body.overrides().entrySet().stream().anyMatch(e->e.getValue()==null||!PermissionCatalog.known(e.getKey())||e.getKey().startsWith("security.")))throw ApiError.bad("استثنای مجوز نامعتبر است؛ امنیت حساب همیشه فعال می‌ماند");
    var effective=new TreeSet<>(rolePermissions(body.roleId()));
    for(var entry:body.overrides().entrySet())if(entry.getValue())effective.add(entry.getKey());else effective.remove(entry.getKey());
    effective=new TreeSet<>(PermissionCatalog.normalize(effective));validatePermissions(base,effective);
    if(base.equals("ADMIN")&&!body.overrides().isEmpty())throw ApiError.bad("مجوزهای مدیر سامانه ثابت هستند");
    if(base.equals("STYLIST")){
      if(body.staffId()==null)throw ApiError.bad("برای زیباکار، پروندهٔ پرسنلی همان شعبه را انتخاب کنید");
      db.one("select id from staff where id=? and branch_id=? and active",body.staffId(),body.branchId());
    }else if(body.staffId()!=null)throw ApiError.bad("اتصال پرسنلی فقط برای حساب زیباکار مجاز است");
    if(base.equals("CUSTOMER")){
      if(body.customerId()==null)throw ApiError.bad("پروندهٔ مشتری را انتخاب کنید");
      db.one("select id from customers where id=? and branch_id=? and active",body.customerId(),body.branchId());
    }else if(body.customerId()!=null)throw ApiError.bad("اتصال مشتری فقط برای حساب مشتری مجاز است");
    if(old!=null){
      version(Db.number(old,"access_version"),body.version());
      if(id==a.uid()&&(!body.active()||body.roleId()!=Db.number(old,"access_role_id")||body.branchId()!=Db.number(old,"branch_id")||!body.overrides().isEmpty()))throw ApiError.bad("دسترسی و وضعیت حساب جاری را نمی‌توانید تغییر دهید");
      if(old.get("role").equals("ADMIN")&&(!body.active()||!base.equals("ADMIN"))&&db.id("select count(*) from app_users where role='ADMIN' and active and id<>?",id)==0)throw ApiError.bad("آخرین مدیر سامانه باید فعال بماند");
      if(old.get("role").equals("CUSTOMER")!=base.equals("CUSTOMER"))throw ApiError.bad("حساب مشتری قابل تبدیل به حساب کارکنان نیست");
      if(old.get("customer_id")!=null&&(!Objects.equals(((Number)old.get("customer_id")).longValue(),body.customerId())||body.branchId()!=Db.number(old,"branch_id")))throw ApiError.bad("مالک پرونده و شعبهٔ حساب مشتری قابل تغییر نیست");
      db.run("update app_users set name=?,username=?,role=?,access_role_id=?,branch_id=?,staff_id=?,customer_id=?,active=?,access_version=access_version+1,failed_attempts=0,locked_until=null where id=?",body.name().trim(),body.username().trim(),base,body.roleId(),body.branchId(),body.staffId(),body.customerId(),body.active(),id);
    }else{
      password(body.password());
      id=db.id("insert into app_users(name,username,password_hash,role,access_role_id,branch_id,staff_id,customer_id,active) values(?,?,?,?,?,?,?,?,?) returning id",body.name().trim(),body.username().trim(),encoder.encode(body.password()),base,body.roleId(),body.branchId(),body.staffId(),body.customerId(),body.active());
    }
    db.run("delete from user_permission_overrides where user_id=?",id);
    for(var entry:body.overrides().entrySet())db.run("insert into user_permission_overrides(user_id,permission,allowed) values(?,?,?)",id,entry.getKey(),entry.getValue());
    db.run("update auth_sessions set revoked=true where user_id=?",id);
    a.audit(old==null?"CREATE":"UPDATE","USER",id,"نقش="+role.get("name")+"؛ نسخهٔ دسترسی به‌روز شد");
    return Map.of("id",id,"reauthenticate",id==a.uid());
  }
  @Transactional public Object reset(long id,Password body){lock();target(id,true);password(body.password());db.run("update app_users set password_hash=?,failed_attempts=0,locked_until=null,access_version=access_version+1 where id=?",encoder.encode(body.password()),id);db.run("update auth_sessions set revoked=true where user_id=?",id);a.audit("PASSWORD_RESET","USER",id,null);return Map.of("ok",true,"reauthenticate",id==a.uid());}
  @Transactional public Object revoke(long id){lock();target(id,true);db.run("update auth_sessions set revoked=true where user_id=?",id);a.audit("REVOKE_SESSIONS","USER",id,null);return Map.of("ok",true,"reauthenticate",id==a.uid());}
  public long builtInRole(String code){return db.id("select id from access_roles where built_in and code=?",code);}
  public UserWrite legacyUpdate(long id,String name,String username,String role,long branchId,Long staffId,boolean active){var u=target(id,false);var overrides=new TreeMap<String,Boolean>();if(role.equals(u.get("role")))for(var row:db.list("select permission,allowed from user_permission_overrides where user_id=?",id))overrides.put(row.get("permission").toString(),Boolean.TRUE.equals(row.get("allowed")));return new UserWrite(name,username,null,role.equals(u.get("role"))?Db.number(u,"access_role_id"):builtInRole(role),branchId,staffId,u.get("customer_id")==null?null:Db.number(u,"customer_id"),active,overrides,Db.number(u,"access_version"));}
}
