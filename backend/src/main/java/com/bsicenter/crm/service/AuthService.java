package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.AuthRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
public class AuthService {
  private final Db db;
  private final Access access;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final TransactionTemplate tx;
  private final CorsConfigurationSource cors;
  private final boolean secure;
  private final String dummy;
  private final CaptchaService challenges;

  public AuthService(
      Db db,
      CaptchaService challenges,
      Access access,
      PasswordEncoder passwords,
      JwtEncoder encoder,
      TransactionTemplate tx,
      CorsConfigurationSource cors,
      @Value("${app.cookie-secure}") boolean secure) {
    this.db = db;
    this.challenges=challenges;
    this.access = access;
    this.passwords = passwords;
    this.encoder = encoder;
    this.tx = tx;
    this.cors = cors;
    this.secure = secure;
    this.dummy = passwords.encode(UUID.randomUUID().toString());
  }

  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public Object captcha(HttpServletRequest req,String mode) { return challenges.create(req.getRemoteAddr(),mode); }

  public Object salons() {
    return db.list("select b.id branch_id,b.name branch_name,s.id salon_id,s.name salon_name from branches b join salons s on s.id=b.salon_id where b.active and s.active order by s.id,b.id");
  }

  public static String random() {
    byte[] b = new byte[48];
    new SecureRandom().nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  public void origin(HttpServletRequest req) {
    String o = req.getHeader("Origin");
    if (o == null || cors.getCorsConfiguration(req).checkOrigin(o) == null)
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "مبدأ درخواست مجاز نیست");
  }

  public void cookie(HttpServletResponse response, String value, long age) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from("bsi_refresh", value)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(age)
            .build()
            .toString());
  }

  public Map<String, Object> issue(
      Map<String, Object> user, UUID sid, String refresh, HttpServletResponse response) {
    var now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer("bsicrm")
            .subject(user.get("id").toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .claim("sid", sid.toString())
            .build();
    String token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    cookie(response, refresh, 604800);
    return Map.of("accessToken", token, "expiresIn", 900, "user", publicUser(user));
  }

  public Map<String, Object> publicUser(Map<String, Object> u) {
    Map<String,Object> out = new LinkedHashMap<>();
    out.put("id",u.get("id")); out.put("name",u.get("name")); out.put("username",u.get("username")); out.put("role",u.get("role")); out.put("branchId",u.get("branch_id")); out.put("salonId",u.get("salon_id"));
    if (u.get("customer_id") != null) out.put("customerId", u.get("customer_id"));
    return out;
  }

  public Object login(@Valid Login body, HttpServletRequest req, HttpServletResponse res) {
    origin(req);
    challenges.verify(body.captchaId(), body.captchaAnswer());
    UUID sid = UUID.randomUUID();
    String token = random();
    String agent = Objects.toString(req.getHeader("User-Agent"), "");
    Map<String, Object> u =
        tx.execute(
            s -> {
              var rows =
                  db.list("select * from app_users where username=? for update", body.username());
              if (rows.isEmpty()) {
                passwords.matches(body.password(), dummy);
                return null;
              }
              var user = rows.get(0);
              if (!Boolean.TRUE.equals(user.get("active"))) return null;
              if (user.get("locked_until") != null
                  && ((java.sql.Timestamp) user.get("locked_until"))
                      .toInstant()
                      .isAfter(Instant.now())) return null;
              if (!passwords.matches(body.password(), user.get("password_hash").toString())) {
                db.run(
                    "update app_users set failed_attempts=failed_attempts+1,locked_until=case when"
                        + " failed_attempts>=4 then now()+interval '15 minutes' else null end where"
                        + " id=?",
                    user.get("id"));
                return null;
              }
              db.run(
                  "update app_users set failed_attempts=0,locked_until=null where id=?",
                  user.get("id"));
              db.run(
                  "insert into"
                      + " auth_sessions(id,user_id,token_hash,expires_at,user_agent,remote_address)"
                      + " values(?,?,?,now()+interval '7 days',?,?)",
                  sid,
                  user.get("id"),
                  hash(token),
                  agent.substring(0, Math.min(300, agent.length())),
                  req.getRemoteAddr());
              db.run(
                  "insert into audit_logs(branch_id,user_id,action,entity,entity_id)"
                      + " values(?,?,'LOGIN','USER',?)",
                  user.get("branch_id"),
                  user.get("id"),
                  user.get("id"));
              return user;
            });
    if (u == null)
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "نام کاربری یا رمز نادرست است؛ پس از تلاش‌های ناموفق، ۱۵ دقیقه صبر کنید");
    return issue(u, sid, token, res);
  }

  public Object register(Register body, HttpServletRequest req, HttpServletResponse res) {
    origin(req); challenges.verify(body.captchaId(), body.captchaAnswer());
    String mobile = com.bsicenter.crm.validation.CustomerIdentity.phone(body.mobile());
    return tx.execute(s -> {
      var selected=db.one("select b.*,s.expires_on,s.active salon_active from branches b join salons s on s.id=b.salon_id where b.id=? and b.active",body.branchId());
      if(!Boolean.TRUE.equals(selected.get("salon_active")) || ((java.sql.Date)selected.get("expires_on")).toLocalDate().isBefore(LocalDate.now(ZoneId.of("Asia/Tehran")))) throw ApiError.bad("سالن در حال حاضر ثبت‌نام نمی‌پذیرد");
      var existing = db.list("select id from customers where salon_id=? and mobile=?", selected.get("salon_id"),mobile);
      if (!existing.isEmpty()) throw ApiError.conflict("این شماره موبایل قبلاً ثبت شده است");
      long branch = body.branchId();
      long customer = db.id("insert into customers(branch_id,first_name,last_name,mobile) values(?,?,?,?) returning id", branch, body.firstName().trim(), body.lastName().trim(), mobile);
      db.run("insert into app_users(username,password_hash,name,role,branch_id,customer_id) values(?,?,?,?,?,?)", "customer:"+selected.get("salon_id")+":"+mobile, passwords.encode(body.password()), body.firstName().trim()+" "+body.lastName().trim(), "CUSTOMER", branch, customer);
      var u = db.one("select * from app_users where customer_id=?", customer);
      UUID sid=UUID.randomUUID(); String token=random();
      db.run("insert into auth_sessions(id,user_id,token_hash,expires_at,user_agent,remote_address) values(?,?,?,now()+interval '7 days',?,?)", sid,u.get("id"),hash(token),Objects.toString(req.getHeader("User-Agent"),""),req.getRemoteAddr());
      return issue(u,sid,token,res);
    });
  }

  public Object customerLogin(CustomerLogin body, HttpServletRequest req, HttpServletResponse res) {
    origin(req); challenges.verify(body.captchaId(), body.captchaAnswer());
    String mobile=com.bsicenter.crm.validation.CustomerIdentity.phone(body.mobile());
    var u=tx.execute(state -> {
      var rows=db.list("select u.* from app_users u join customers c on c.id=u.customer_id join branches b on b.id=? where c.mobile=? and c.salon_id=b.salon_id and u.role='CUSTOMER' and u.active and c.active for update of u",body.branchId(),mobile);
      if(rows.isEmpty()){passwords.matches(body.password(),dummy);return null;}
      var user=rows.get(0);
      if(user.get("locked_until")!=null && ((java.sql.Timestamp)user.get("locked_until")).toInstant().isAfter(Instant.now())) return null;
      if(!passwords.matches(body.password(),user.get("password_hash").toString())){
        db.run("update app_users set failed_attempts=failed_attempts+1,locked_until=case when failed_attempts>=4 then now()+interval '15 minutes' else null end where id=?",user.get("id"));return null;
      }
      db.run("update app_users set failed_attempts=0,locked_until=null where id=?",user.get("id"));return user;
    });
    if(u==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"موبایل یا رمز نادرست است؛ پس از تلاش‌های ناموفق ۱۵ دقیقه صبر کنید");
    UUID sid=UUID.randomUUID(); String token=random();
    String agent=Objects.toString(req.getHeader("User-Agent"),"");
    db.run("insert into auth_sessions(id,user_id,token_hash,expires_at,user_agent,remote_address) values(?,?,?,now()+interval '7 days',?,?)",sid,u.get("id"),hash(token),agent.substring(0,Math.min(300,agent.length())),req.getRemoteAddr());
    return issue(u,sid,token,res);
  }

  public Object refresh(String token, HttpServletRequest req, HttpServletResponse res) {
    origin(req);
    return tx.execute(
        s -> {
          var rows =
              db.list(
                  "select s.id as session_id,u.* from auth_sessions s join app_users u on"
                      + " u.id=s.user_id where s.token_hash=? and not s.revoked and"
                      + " s.expires_at>now() and u.active for update of s",
                  hash(token));
          if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "دوباره وارد شوید");
          var u = rows.get(0);
          String next = random();
          UUID sid = (UUID) u.get("session_id");
          db.run(
              "update auth_sessions set token_hash=?,refreshed_at=now() where id=?",
              hash(next),
              sid);
          return issue(u, sid, next, res);
        });
  }

  public Object logout(String token, HttpServletRequest req, HttpServletResponse res) {
    origin(req);
    db.run("update auth_sessions set revoked=true where token_hash=?", hash(token));
    cookie(res, "", 0);
    return Map.of("ok", true);
  }

  public Object me() {
    var result=publicUser(access.user()); result.put("branchId",access.branch()); result.put("salonId",access.salon()); return result;
  }

  public Object password(@Valid Password b) {
    var user = access.user();
    long id = Db.number(user, "id");
    tx.executeWithoutResult(
        state -> {
          var locked = db.one("select * from app_users where id=? for update", id);
          if (!passwords.matches(b.currentPassword(), locked.get("password_hash").toString()))
            throw ApiError.bad("رمز فعلی درست نیست");
          db.run(
              "update app_users set password_hash=?,failed_attempts=0,locked_until=null where id=?",
              passwords.encode(b.newPassword()),
              id);
          access.audit("PASSWORD_CHANGED", "USER", id, null);
          db.run("update auth_sessions set revoked=true where user_id=?", id);
        });
    return Map.of("ok", true);
  }

  public Object sessions() {
    long uid = access.uid();
    var jwt =
        (Jwt)
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    return db.list(
        "select id,created_at,expires_at,refreshed_at,user_agent,remote_address,(id=?) is_current"
            + " from auth_sessions where user_id=? and not revoked and expires_at>now() order by"
            + " created_at desc",
        UUID.fromString(jwt.getClaimAsString("sid")),
        uid);
  }

  public Object revoke(UUID id) {
    long uid = access.uid();
    int affected =
        db.run("update auth_sessions set revoked=true where id=? and user_id=?", id, uid);
    if (affected == 0) throw ApiError.missing();
    return Map.of("ok", true);
  }
}
