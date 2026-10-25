package com.bsicenter.crm.controller;

import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import com.bsicenter.crm.exception.ApiError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenancy")
public class TenancyController {
  final Db db; final Access a; final PasswordEncoder passwords;
  public TenancyController(Db db,Access a,PasswordEncoder p){this.db=db;this.a=a;this.passwords=p;}
  public record Salon(@NotBlank @Size(max=160) String name,@NotBlank @Size(max=120) String branchName,
    @NotBlank @Size(max=160) String managerName,@NotBlank @Size(max=80) String username,
    @NotBlank @Size(min=12,max=72) String password,@NotNull @Pattern(regexp="BASIC|STANDARD|ADVANCED|SMART") String plan,@NotNull LocalDate expiresOn){}
  public record SalonEdit(@NotBlank @Size(max=160) String name,boolean active){}
  public record Branch(@NotBlank @Size(max=120) String name,@Size(max=20) String phone,@Size(max=500) String address,boolean active,@NotNull Long salonId){}
  public record Switch(@Positive long branchId){}
  public record UserEdit(@NotBlank @Size(max=160) String name,@NotBlank @Size(max=80) String username,
    @NotNull @Pattern(regexp="ADMIN|MANAGER|RECEPTION|STYLIST|MARKETING|FINANCE|CUSTOMER") String role,
    @Positive long branchId,Long staffId,boolean active){}
  public record Password(@NotBlank @Size(min=12,max=72) String password){}

  @GetMapping("/salons") public Object salons(){
    a.roles("ADMIN","MANAGER");
    return a.role().equals("ADMIN") ? db.list("select * from salons order by id") : db.list("select * from salons where id=?",a.salon());
  }
  @PostMapping("/salons") @Transactional public Object create(@Valid @RequestBody Salon b){
    a.roles("ADMIN");
    long salon=db.id("insert into salons(name,plan,expires_on) values(?,?,?) returning id",b.name().trim(),b.plan(),b.expiresOn());
    long branch=db.id("insert into branches(name,salon_id) values(?,?) returning id",b.branchName().trim(),salon);
    db.run("insert into accounts(branch_id,name) values(?,'صندوق اصلی')",branch);
    long manager=db.id("insert into app_users(name,username,password_hash,role,branch_id) values(?,?,?,'MANAGER',?) returning id",b.managerName().trim(),b.username().trim(),passwords.encode(b.password()),branch);
    a.audit("CREATE","SALON",salon,"شناسه مدیر="+manager);
    return Map.of("id",salon,"branchId",branch,"managerId",manager);
  }
  @PutMapping("/salons/{id}") @Transactional public Object editSalon(@PathVariable long id,@Valid @RequestBody SalonEdit b){
    a.roles("ADMIN");db.one("select id from salons where id=? for update",id);
    db.run("update salons set name=?,active=? where id=?",b.name().trim(),b.active(),id);
    a.audit("UPDATE","SALON",id,null);return Map.of("ok",true);
  }
  private void salonScope(long id){
    a.roles("ADMIN","MANAGER");
    if(!a.role().equals("ADMIN") && id!=a.salon()) throw ApiError.missing();
    db.one("select id from salons where id=?",id);
  }
  @GetMapping("/branches") public Object branches(){
    a.roles("ADMIN","MANAGER");
    String base="select b.*,s.name salon_name from branches b join salons s on s.id=b.salon_id";
    return a.role().equals("ADMIN")?db.list(base+" order by b.id"):db.list(base+" where b.salon_id=? order by b.id",a.salon());
  }
  @PostMapping("/branches") @Transactional public Object createBranch(@Valid @RequestBody Branch b){
    salonScope(b.salonId());a.requireSubscription();
    long id=db.id("insert into branches(name,phone,address,active,salon_id) values(?,?,?,?,?) returning id",b.name().trim(),Objects.toString(b.phone(),""),Objects.toString(b.address(),""),b.active(),b.salonId());
    db.run("insert into accounts(branch_id,name) values(?,'صندوق اصلی')",id);
    a.audit("CREATE","BRANCH",id,null);return Map.of("id",id);
  }
  @PutMapping("/branches/{id}") @Transactional public Object editBranch(@PathVariable long id,@Valid @RequestBody Branch b){
    salonScope(b.salonId());a.requireSubscription();
    db.one("select id from salons where id=? for update",b.salonId());
    db.one("select id from branches where id=? and salon_id=? for update",id,b.salonId());
    if(!b.active() && db.id("select count(*) from branches where salon_id=? and id<>? and active",b.salonId(),id)==0) throw ApiError.bad("حداقل یک شعبه باید فعال بماند");
    db.run("update branches set name=?,phone=?,address=?,active=? where id=?",b.name().trim(),Objects.toString(b.phone(),""),Objects.toString(b.address(),""),b.active(),id);
    a.audit("UPDATE","BRANCH",id,null);return Map.of("ok",true);
  }
  @PostMapping("/switch") @Transactional public Object switchBranch(@Valid @RequestBody Switch b){
    a.roles("ADMIN","MANAGER");
    var branch=db.one("select * from branches where id=?",b.branchId());salonScope(Db.number(branch,"salon_id"));
    if(!a.role().equals("ADMIN") && !Boolean.TRUE.equals(branch.get("active"))) throw ApiError.bad("شعبه غیرفعال است");
    var jwt=(Jwt)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    db.run("update auth_sessions set active_branch_id=? where id=? and user_id=?",b.branchId(),UUID.fromString(jwt.getClaimAsString("sid")),a.uid());
    a.audit("SWITCH","BRANCH",b.branchId(),null);return Map.of("ok",true);
  }
  private Map<String,Object> managedUser(long id){
    a.roles("ADMIN","MANAGER");a.requireSubscription();
    var u=db.one("select * from app_users where id=? for update",id);
    if(!a.role().equals("ADMIN") && (Db.number(u,"salon_id")!=a.salon() || Set.of("ADMIN","MANAGER").contains(u.get("role")))) throw ApiError.missing();
    return u;
  }
  @GetMapping("/staff") public Object staff(@RequestParam long branchId){
    a.roles("ADMIN","MANAGER");a.requireSubscription();
    var branch=db.one("select salon_id from branches where id=?",branchId);salonScope(Db.number(branch,"salon_id"));
    return db.list("select id,name from staff where branch_id=? and active order by name",branchId);
  }
  @GetMapping("/users") public Object users(){
    a.roles("ADMIN","MANAGER");a.requireSubscription();
    String sql="select u.id,u.name,u.username,u.role,u.branch_id,u.salon_id,u.staff_id,u.customer_id,u.active,u.locked_until,b.name branch_name from app_users u join branches b on b.id=u.branch_id";
    return a.role().equals("ADMIN")?db.list(sql+" order by u.id"):db.list(sql+" where u.salon_id=? and u.role<>'ADMIN' order by u.id",a.salon());
  }
  @PutMapping("/users/{id}") @Transactional public Object updateUser(@PathVariable long id,@Valid @RequestBody UserEdit b){
    db.jdbc.execute("select pg_advisory_xact_lock(741004)");
    var u=managedUser(id);
    var branch=db.one("select * from branches where id=? and active",b.branchId());salonScope(Db.number(branch,"salon_id"));
    if(!a.role().equals("ADMIN") && Set.of("ADMIN","MANAGER").contains(b.role())) throw ApiError.bad("ایجاد این نقش فقط توسط مدیر سامانه مجاز است");
    if(id==a.uid() && (!b.active() || !b.role().equals(u.get("role")))) throw ApiError.bad("نقش یا وضعیت حساب جاری را نمی‌توانید تغییر دهید");
    if(u.get("role").equals("ADMIN") && (!b.active() || !b.role().equals("ADMIN")) && db.id("select count(*) from app_users where role='ADMIN' and active and id<>?",id)==0) throw ApiError.bad("آخرین مدیر سامانه باید فعال بماند");
    if(u.get("role").equals("CUSTOMER") != b.role().equals("CUSTOMER")) throw ApiError.bad("نقش حساب متصل به مشتری قابل تبدیل نیست");
    if(u.get("customer_id")!=null && b.branchId()!=Db.number(u,"branch_id")) throw ApiError.bad("شعبهٔ حساب مشتری باید با پروندهٔ او یکسان باشد");
    if(b.role().equals("STYLIST") && b.staffId()==null) throw ApiError.bad("پرسنل آرایشگر را انتخاب کنید");
    if(b.staffId()!=null) db.one("select id from staff where id=? and branch_id=?",b.staffId(),b.branchId());
    db.run("update app_users set name=?,username=?,role=?,branch_id=?,staff_id=?,active=?,failed_attempts=0,locked_until=null where id=?",b.name().trim(),b.username().trim(),b.role(),b.branchId(),b.staffId(),b.active(),id);
    db.run("update auth_sessions set revoked=true where user_id=?",id);a.audit("UPDATE","USER",id,null);
    return Map.of("ok",true,"reauthenticate",id==a.uid());
  }
  @PostMapping("/users/{id}/password") @Transactional public Object reset(@PathVariable long id,@Valid @RequestBody Password b){
    managedUser(id);db.run("update app_users set password_hash=?,failed_attempts=0,locked_until=null where id=?",passwords.encode(b.password()),id);
    db.run("update auth_sessions set revoked=true where user_id=?",id);a.audit("PASSWORD_RESET","USER",id,null);return Map.of("ok",true);
  }
  @PostMapping("/users/{id}/revoke") @Transactional public Object revoke(@PathVariable long id){
    managedUser(id);db.run("update auth_sessions set revoked=true where user_id=?",id);a.audit("REVOKE_SESSIONS","USER",id,null);return Map.of("ok",true);
  }
  @PostMapping("/customers/{id}/account") @Transactional public Object customerAccount(@PathVariable long id,@Valid @RequestBody Password b){
    a.roles("ADMIN","MANAGER");a.requireSubscription();
    var c=db.one("select * from customers where id=? and branch_id=? and active for update",id,a.branch());
    if(!db.list("select id from app_users where customer_id=?",id).isEmpty()) throw ApiError.conflict("این مشتری حساب دارد؛ از مدیریت کاربران رمز را تغییر دهید");
    long uid=db.id("insert into app_users(username,name,password_hash,role,branch_id,customer_id) values(?,?,?,'CUSTOMER',?,?) returning id","customer:"+c.get("salon_id")+":"+id,c.get("first_name")+" "+c.get("last_name"),passwords.encode(b.password()),a.branch(),id);
    a.audit("CREATE_ACCOUNT","CUSTOMER",id,null);return Map.of("id",uid);
  }
}
