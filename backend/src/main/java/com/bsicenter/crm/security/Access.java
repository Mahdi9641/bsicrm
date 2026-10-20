package com.bsicenter.crm.security;

import com.bsicenter.crm.repository.Db;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Access {
  final Db db;

  public Access(Db db) {
    this.db = db;
  }

  public Map<String, Object> user() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ابتدا وارد سامانه شوید");
    var rows =
        db.list(
            "select u.*,s.active_branch_id from app_users u join auth_sessions s on s.user_id=u.id where u.id=? and"
                + " s.id=? and u.active and not s.revoked and s.expires_at>now() and (u.customer_id is null or exists(select 1 from customers c where c.id=u.customer_id and c.active and c.branch_id=u.branch_id))",
            Long.valueOf(jwt.getSubject()),
            UUID.fromString(jwt.getClaimAsString("sid")));
    if (rows.isEmpty())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "نشست منقضی شده است");
    return rows.get(0);
  }

  public long uid() {
    return Db.number(user(), "id");
  }

  public long branch() {
    var u = user();
    long home = Db.number(u, "branch_id");
    if (u.get("active_branch_id") == null) return home;
    long selected = Db.number(u, "active_branch_id");
    if (u.get("role").equals("ADMIN")) return selected;
    if (u.get("role").equals("MANAGER") && !db.list("select id from branches where id=? and salon_id=?", selected, u.get("salon_id")).isEmpty()) return selected;
    return home;
  }

  public String role() {
    return user().get("role").toString();
  }

  public boolean stylist() {
    return role().equals("STYLIST");
  }

  public long customerId() { return Db.number(user(), "customer_id"); }

  public boolean customer() { return role().equals("CUSTOMER"); }

  public long staffId() {
    return Db.number(user(), "staff_id");
  }

  public void roles(String... roles) {
    if (!Set.of(roles).contains(role()))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "دسترسی این بخش برای نقش شما فعال نیست");
  }

  public void operations() {
    roles("ADMIN", "MANAGER", "RECEPTION");
  }

  public void finance() {
    roles("ADMIN", "MANAGER", "FINANCE");
  }

  public void sales() {
    roles("ADMIN", "MANAGER", "FINANCE", "RECEPTION");
  }

  public void marketing() {
    roles("ADMIN", "MANAGER", "MARKETING");
  }

  public long salon() {
    return Db.number(db.one("select salon_id from branches where id=?", branch()), "salon_id");
  }

  public Map<String,Object> settings() {
    return db.one("select s.*,s.name organization,'Asia/Tehran' timezone,'TOMAN' currency from salons s where s.id=?", salon());
  }

  public static boolean expired(java.time.LocalDate end, java.time.LocalDate today) {
    return end == null || end.isBefore(today);
  }

  public void requireSubscription() {
    if (role().equals("ADMIN")) return;
    var s=settings();
    var branchRow=db.one("select active from branches where id=?",branch());
    if (!Boolean.TRUE.equals(s.get("active")) || !Boolean.TRUE.equals(branchRow.get("active")))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"سالن یا شعبه غیرفعال است؛ با مدیر سامانه تماس بگیرید");
    java.time.LocalDate end=s.get("expires_on")==null?null:((java.sql.Date)s.get("expires_on")).toLocalDate();
    if (expired(end, java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tehran"))))
      throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"اشتراک سالن منقضی شده است؛ مدیر سالن باید اشتراک را تمدید کند");
  }

  public void plan(String minimum) {
    requireSubscription();
    var plans=List.of("BASIC","STANDARD","ADVANCED","SMART");
    if (plans.indexOf(settings().get("plan")) < plans.indexOf(minimum))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"این قابلیت در پلن فعلی فعال نیست");
  }

  public void audit(String action, String entity, long id, String detail) {
    db.run(
        "insert into audit_logs(branch_id,user_id,action,entity,entity_id,detail)"
            + " values(?,?,?,?,?,?)",
        branch(),
        uid(),
        action,
        entity,
        id,
        detail);
  }
}
