package com.bsicenter.crm.controller;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import com.bsicenter.crm.service.BillingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController @RequestMapping("/api/billing")
public class BillingController {
  final BillingService service;final Access a;final Db db;
  public BillingController(BillingService service,Access a,Db db){this.service=service;this.a=a;this.db=db;}
  public record Order(@NotNull @Pattern(regexp="BASIC|STANDARD|ADVANCED|SMART") String plan,@NotNull @Pattern(regexp="MANUAL|ZARINPAL") String method,@NotNull UUID idempotencyKey){}
  public record Reference(@NotBlank @Size(max=120) String reference){}
  public record Review(boolean approve,@Size(max=120) String reference){}
  public record License(@NotNull @Pattern(regexp="BASIC|STANDARD|ADVANCED|SMART") String plan,@NotNull LocalDate expiresOn){}
  public record Plan(@NotBlank @Size(max=100) String name,@NotBlank @Size(max=500) String description,@NotNull @DecimalMin("1000") @DecimalMax("1000000000") BigDecimal price,@Min(1) @Max(730) int durationDays,boolean active){}
  @GetMapping("/status") public Object status(){return service.status();}
  @PostMapping("/orders") public Object order(@Valid @RequestBody Order b){return service.order(b.plan(),b.method(),b.idempotencyKey());}
  @PostMapping("/orders/{id}/pay") public Object pay(@PathVariable UUID id){return service.pay(id);}
  @PostMapping("/orders/{id}/reference") public Object reference(@PathVariable UUID id,@Valid @RequestBody Reference b){return service.reference(id,b.reference());}
  @PostMapping("/orders/{id}/review") public Object review(@PathVariable UUID id,@Valid @RequestBody Review b){return service.review(id,b.approve(),b.reference());}
  @GetMapping("/orders") public Object orders(){a.roles("ADMIN");return db.list("select o.*,s.name salon_name from subscription_orders o join salons s on s.id=o.salon_id order by o.created_at desc limit 300");}
  @GetMapping("/plans") public Object plans(){a.roles("ADMIN");return db.list("select * from subscription_plans order by price");}
  @PutMapping("/plans/{code}") @Transactional public Object plan(@PathVariable String code,@Valid @RequestBody Plan b){
    a.roles("ADMIN");db.one("select code from subscription_plans where code=? for update",code);
    db.run("update subscription_plans set name=?,description=?,price=?,duration_days=?,active=? where code=?",b.name().trim(),b.description().trim(),b.price(),b.durationDays(),b.active(),code);
    a.audit("UPDATE","PLAN",0,code);return Map.of("ok",true);
  }
  @PutMapping("/salons/{id}/license") @Transactional public Object license(@PathVariable long id,@Valid @RequestBody License b){
    a.roles("ADMIN");db.one("select id from salons where id=? for update",id);
    db.run("update salons set plan=?,expires_on=? where id=?",b.plan(),b.expiresOn(),id);a.audit("LICENSE","SALON",id,b.plan()+" / "+b.expiresOn());return Map.of("ok",true);
  }
  @GetMapping("/callback") public ResponseEntity<?> callback(@RequestParam(name="Authority") @Size(max=100) String authority,@RequestParam(name="Status",defaultValue="NOK") String status){
    // Never trust Status, amount or plan from the browser. Server-to-server verification decides.
    String result="cancelled";
    if(status.equals("OK")){try{service.verify(authority);result="verified";}catch(Exception e){result="unverified";}}
    return ResponseEntity.status(303).header("Location","/subscription?payment="+result).build();
  }
}
