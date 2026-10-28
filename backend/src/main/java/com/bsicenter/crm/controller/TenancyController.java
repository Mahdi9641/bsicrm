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
  final com.bsicenter.crm.service.AccessManagementService iam;
  final Db db; final Access a; final PasswordEncoder passwords;
  public TenancyController(Db db,Access a,PasswordEncoder p,com.bsicenter.crm.service.AccessManagementService iam){this.iam=iam;this.db=db;this.a=a;this.passwords=p;}
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
    return iam.users();
  }
  @PutMapping("/users/{id}") @Transactional public Object updateUser(@PathVariable long id,@Valid @RequestBody UserEdit b){
    return iam.saveUser(id,iam.legacyUpdate(id,b.name(),b.username(),b.role(),b.branchId(),b.staffId(),b.active()));
  }
  @PostMapping("/users/{id}/password") @Transactional public Object reset(@PathVariable long id,@Valid @RequestBody Password b){
    return iam.reset(id,new com.bsicenter.crm.service.AccessManagementService.Password(b.password()));
  }
  @PostMapping("/users/{id}/revoke") @Transactional public Object revoke(@PathVariable long id){
    return iam.revoke(id);
  }
  @PostMapping("/customers/{id}/account") @Transactional public Object customerAccount(@PathVariable long id,@Valid @RequestBody Password b){
    a.permission("users.write");
    var c=db.one("select * from customers where id=? and branch_id=? and active",id,a.branch());
    return iam.saveUser(null,new com.bsicenter.crm.service.AccessManagementService.UserWrite(c.get("first_name")+" "+c.get("last_name"),"customer:"+c.get("salon_id")+":"+id,b.password(),iam.builtInRole("CUSTOMER"),a.branch(),null,id,true,Map.of(),0L));
  }
}
