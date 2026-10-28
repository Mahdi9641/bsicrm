package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.CatalogRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class CatalogService {
  private final Db db;
  private final AccessManagementService iam;
  private final Access a;
  private final PasswordEncoder passwords;

  public CatalogService(Db db, Access a, PasswordEncoder passwords, AccessManagementService iam) {
    this.db = db; this.iam=iam;
    this.a = a;
    this.passwords = passwords;
  }

  public Map<String, Object> scoped(String table, long id) {
    return db.one("select * from " + table + " where id=? and branch_id=?", id, a.branch());
  }

  public Object bootstrap() {
    var user=a.user();var settings=a.settings();var branch=db.one("select * from branches where id=?",a.branch());
    boolean valid=Boolean.TRUE.equals(settings.get("active")) && Boolean.TRUE.equals(branch.get("active")) && !((java.sql.Date)settings.get("expires_on")).toLocalDate().isBefore(LocalDate.now(ZoneId.of("Asia/Tehran")));
    Map<String,Object> visibleSettings=settings;
    if(!a.has("settings.read")){
      visibleSettings=new LinkedHashMap<>();for(String key:List.of("id","name","organization","plan","expires_on","active","timezone","currency"))visibleSettings.put(key,settings.get(key));
    }
    return Map.of("settings",visibleSettings,"branch",branch,"subscriptionActive",valid,"permissions",a.policy().permissions(),"menus",a.policy().menus(),
      "accounts",a.has("finance.read")||a.has("invoices.write")?db.list("select id,name from accounts where branch_id=? and active",a.branch()):List.of());
  }
  public Object lookups(String q,Long customerId){
    if(q.length()>100)throw ApiError.bad("متن جستجو طولانی است");
    a.user();long branch=a.branch();
    boolean operations=a.has("appointments.write")||a.has("visits.write")||a.has("invoices.write");
    boolean catalog=operations||a.has("staff.write")||a.has("commission-rules.read");
    return Map.of(
      "customers",operations||a.has("customers.read")||a.has("tasks.write")||a.has("events.write")?db.list("select id,first_name,last_name,mobile from customers where branch_id=? and active and (cast(? as bigint) is null or id=?) and (first_name||' '||last_name ilike ? or mobile like ?) order by id desc limit 100",branch,customerId,customerId,"%"+q+"%","%"+q+"%"):List.of(),
      "services",catalog||a.has("services.read")?db.list("select id,name,category,price,duration_minutes,buffer_minutes,active from services where branch_id=? and active order by name",branch):List.of(),
      "staff",catalog?db.list("select t.id,t.name,t.work_start,t.work_end,t.work_days,t.active,coalesce((select json_agg(service_id)::text from staff_services where staff_id=t.id),'[]') service_ids from staff t where t.branch_id=? and t.active"+(a.stylist()?" and t.id="+a.staffId():""),branch):List.of(),
      "products",a.has("invoices.write")||a.has("products.read")?db.list("select id,name,price,stock,active from products where branch_id=? and active",branch):List.of(),
      "accounts",a.has("finance.read")||a.has("invoices.write")?db.list("select id,name from accounts where branch_id=? and active",branch):List.of());
  }

  public Object customers(String q, int page) {
    a.permission("customers.read");
    if (q.length() > 100 || page < 0) throw ApiError.bad("جستجو معتبر نیست");
    q = com.bsicenter.crm.validation.CustomerIdentity.digits(q);
    if (q.startsWith("+98") || q.startsWith("0098"))
      q = com.bsicenter.crm.validation.CustomerIdentity.phone(q);
    var rows = db.list(
        "select c.*,coalesce((select sum(points) from loyalty_entries l where"
            + " l.customer_id=c.id),0) points,coalesce((select sum(total) from invoices i where"
            + " i.customer_id=c.id and i.status<>'VOID'),0) total_spend,(select max(check_in_at)"
            + " from visits v where v.customer_id=c.id and v.status='COMPLETED') last_visit from"
            + " customers c where branch_id=? and (first_name||' '||last_name ilike ? or mobile"
            + " like ? or national_code like ? or customer_code ilike ?) order by id desc limit 100"
            + " offset ?",
        a.branch(),
        "%" + q + "%",
        "%" + q + "%",
        "%" + q + "%",
        "%" + q + "%",
        page * 100);
    for(var row:rows){if(!a.has("invoices.read"))row.remove("total_spend");if(!a.has("loyalty.read"))row.remove("points");}
    return rows;
  }

  public Object customer(long id) {
    a.permission("customers.read");
    var c = scoped("customers", id);
    return Map.of(
        "customer",
        c,
        "summary",
        customerSummary(id),
        "relations",
        db.list("select * from customer_relations where customer_id=? order by id", id),
        "appointments",
        a.has("appointments.read")?db.list(
            "select * from appointments where customer_id=? order by starts_at desc limit 100", id):List.of(),
        "invoices",
        a.has("invoices.read")?db.list(
            "select i.*,coalesce((select sum(p.amount) from payments p where p.invoice_id=i.id and"
                + " not reversed),0) paid from invoices i where customer_id=? order by id desc"
                + " limit 100",
            id):List.of(),
        "preferences",
        db.list("select * from customer_preferences where customer_id=?", id),
        "occasions",
        db.list("select * from customer_occasions where customer_id=?", id),
        "loyalty",
        a.has("loyalty.read")?db.list(
            "select * from loyalty_entries where customer_id=? order by id desc limit 100", id):List.of());
  }

  private Object customerSummary(long id) {
    var summary =
        db.one(
            """
select
(select count(*) from visits where customer_id=c.id and status='COMPLETED') visits,
(select min(check_in_at) from visits where customer_id=c.id and status='COMPLETED') first_visit,
(select max(check_in_at) from visits where customer_id=c.id and status='COMPLETED') last_visit,
(select count(*) from visits where customer_id=c.id and status='COMPLETED' and check_in_at>=now()-interval '365 days') frequency,
coalesce((select sum(total) from invoices where customer_id=c.id and status<>'VOID'),0) total_spend,
coalesce((select sum(i.total-coalesce((select sum(p.amount) from payments p where p.invoice_id=i.id and not p.reversed),0)) from invoices i where i.customer_id=c.id and i.status<>'VOID'),0) debt,
coalesce((select sum(points) from loyalty_entries where customer_id=c.id),0) points,
(select count(*) from appointments where customer_id=c.id and status='CANCELLED') cancelled,
(select count(*) from appointments where customer_id=c.id and status='NO_SHOW') no_shows,
(select min(starts_at) from appointments where customer_id=c.id and status in ('BOOKED','CONFIRMED') and starts_at>now()) next_appointment,
coalesce((select sum(ii.total-ii.unit_cost*ii.quantity-coalesce((select sum(co.amount) from commissions co where co.invoice_item_id=ii.id),0)) from invoice_items ii join invoices i on i.id=ii.invoice_id where i.customer_id=c.id and i.status<>'VOID'),0) contribution,
c.created_at from customers c where c.id=?
""",
            id);
    long visits = Db.number(summary, "visits"), points = Db.number(summary, "points");
    summary.put(
        "average_spend",
        visits == 0
            ? java.math.BigDecimal.ZERO
            : Db.money(summary, "total_spend")
                .divide(java.math.BigDecimal.valueOf(visits), 0, java.math.RoundingMode.HALF_UP));
    summary.put("tier", points >= 100 ? "GOLD" : points >= 30 ? "SILVER" : "BRONZE");
    var last =
        (java.sql.Timestamp)
            summary.get(summary.get("last_visit") == null ? "created_at" : "last_visit");
    long elapsed = Duration.between(last.toInstant(), Instant.now()).toDays();
    var settings = a.settings();
    long risk = Db.number(settings, "risk_days");
    summary.put(
        "segment",
        elapsed > 2 * risk
            ? "LOST"
            : elapsed > risk
                ? "AT_RISK"
                : Db.number(summary, "frequency") >= Db.number(settings, "loyal_visits")
                    ? "LOYAL"
                    : "GROWING");
    if(!a.has("invoices.read"))for(String key:List.of("total_spend","average_spend","debt"))summary.remove(key);
    if(!a.has("finance.read"))summary.remove("contribution");
    if(!a.has("loyalty.read"))for(String key:List.of("points","tier"))summary.remove(key);
    if(!a.has("rfm.read"))summary.remove("segment");
    if(!a.has("appointments.read"))for(String key:List.of("cancelled","no_shows","next_appointment"))summary.remove(key);
    return summary;
  }

  @Transactional
  public Object relation(long id, Relation body) {
    a.permission("customers.write");
    scoped("customers", id);
    if (body.birthDate() != null && body.birthDate().isAfter(LocalDate.now()))
      throw ApiError.bad("تاریخ تولد معتبر نیست");
    long relationId =
        db.id(
            "insert into customer_relations(customer_id,relationship,full_name,birth_date,notes)"
                + " values(?,?,?,?,?) returning id",
            id,
            body.relationship(),
            body.fullName().trim(),
            body.birthDate(),
            body.notes());
    a.audit("CREATE", "CUSTOMER_RELATION", relationId, null);
    return Map.of("id", relationId);
  }

  @Transactional
  public Object removeRelation(long id, long relationId) {
    a.permission("customers.write");
    scoped("customers", id);
    if (db.run("delete from customer_relations where id=? and customer_id=?", relationId, id) == 0)
      throw ApiError.missing();
    a.audit("DELETE", "CUSTOMER_RELATION", relationId, null);
    return Map.of("ok", true);
  }

  @Transactional
  public Object createCustomer(@Valid Customer c) {
    a.permission("customers.write");
    return saveCustomer(null, c);
  }

  @Transactional
  public Object editCustomer(long id, @Valid Customer c) {
    a.permission("customers.write");
    scoped("customers", id);
    return saveCustomer(id, c);
  }

  private Object saveCustomer(Long id, Customer c) {
    com.bsicenter.crm.validation.CustomerIdentity.validateNationalCode(c.nationalCode());
    String status = c.status() == null ? (c.active() ? "ACTIVE" : "INACTIVE") : c.status();
    if (!db.list(
            "select id from customers where salon_id=? and mobile=? and (cast(? as bigint) is null or id<>?)",
            a.salon(), c.mobile(),
            id,
            id)
        .isEmpty()) throw ApiError.conflict("شماره موبایل قبلاً برای یک مشتری ثبت شده است");
    if (c.nationalCode() != null
        && !db.list(
                "select id from customers where salon_id=? and national_code=? and (cast(? as bigint) is null or"
                    + " id<>?)",
                a.salon(), c.nationalCode(),
                id,
                id)
            .isEmpty()) throw ApiError.conflict("کد ملی قبلاً برای یک مشتری ثبت شده است");
    if (c.referrerId() != null) {
      scoped("customers", c.referrerId());
      if (c.referrerId().equals(id)) throw ApiError.bad("مشتری نمی‌تواند معرف خودش باشد");
    }
    if (c.preferredStaffId() != null) scoped("staff", c.preferredStaffId());
    if (c.birthDate() != null && c.birthDate().isAfter(LocalDate.now()))
      throw ApiError.bad("تاریخ تولد معتبر نیست");
    if (id == null)
      id =
          db.id(
              "insert into customers(branch_id,first_name,last_name,mobile) values(?,?,?,?)"
                  + " returning id",
              a.branch(),
              c.firstName().trim(),
              c.lastName().trim(),
              c.mobile());
    db.run(
        "update customers set"
            + " first_name=?,last_name=?,mobile=?,alternate_mobile=?,national_code=?,status=?,contact_channel=?,birth_date=?,gender=?,marital_status=?,education=?,occupation=?,address=?,city=?,acquisition_channel=?,referrer_id=?,preferred_staff_id=?,marketing_consent=?,consent_at=case"
            + " when ? and not marketing_consent then now() when not ? then null else consent_at"
            + " end,notes=?,active=? where id=?",
        c.firstName().trim(),
        c.lastName().trim(),
        c.mobile(),
        c.alternateMobile(),
        c.nationalCode(),
        status,
        Objects.requireNonNullElse(c.contactChannel(), "SMS"),
        c.birthDate(),
        c.gender(),
        c.maritalStatus(),
        c.education(),
        c.occupation(),
        c.address(),
        c.city(),
        Objects.requireNonNullElse(c.acquisitionChannel(), "OTHER"),
        c.referrerId(),
        c.preferredStaffId(),
        c.marketingConsent(),
        c.marketingConsent(),
        c.marketingConsent(),
        c.notes(),
        status.equals("ACTIVE"),
        id);
    db.run("update app_users set name=?,active=? where customer_id=?",c.firstName().trim()+" "+c.lastName().trim(),status.equals("ACTIVE"),id);
    if (!status.equals("ACTIVE")) db.run("update auth_sessions set revoked=true where user_id in (select id from app_users where customer_id=?)",id);
    a.audit("SAVE", "CUSTOMER", id, null);
    return scoped("customers", id);
  }

  public Object preference(long id, @Valid Preference p) {
    a.permission("customers.write");
    scoped("customers", id);
    return Map.of(
        "id",
        db.id(
            "insert into customer_preferences(customer_id,category,value) values(?,?,?) returning"
                + " id",
            id,
            p.category(),
            p.value()));
  }

  public Object occasion(long id, @Valid Occasion p) {
    a.permission("customers.write");
    scoped("customers", id);
    return Map.of(
        "id",
        db.id(
            "insert into customer_occasions(customer_id,title,occasion_date) values(?,?,?)"
                + " returning id",
            id,
            p.title(),
            p.date()));
  }

  public Object services() {
    a.permission("services.read");
    a.user();
    return db.list("select * from services where branch_id=? order by id", a.branch());
  }

  @Transactional
  public Object service(@Valid Service s) {
    a.permission("services.write");
    long id =
        db.id(
            "insert into"
                + " services(branch_id,name,category,description,price,material_cost,duration_minutes,buffer_minutes,active)"
                + " values(?,?,?,?,?,?,?,?,?) returning id",
            a.branch(),
            s.name(),
            s.category(),
            s.description(),
            s.price(),
            s.materialCost(),
            s.durationMinutes(),
            s.bufferMinutes(),
            s.active());
    a.audit("CREATE", "SERVICE", id, null);
    return scoped("services", id);
  }

  @Transactional
  public Object serviceEdit(long id, @Valid Service s) {
    a.permission("services.write");
    scoped("services", id);
    db.run(
        "update services set"
            + " name=?,category=?,description=?,price=?,material_cost=?,duration_minutes=?,buffer_minutes=?,active=?"
            + " where id=?",
        s.name(),
        s.category(),
        s.description(),
        s.price(),
        s.materialCost(),
        s.durationMinutes(),
        s.bufferMinutes(),
        s.active(),
        id);
    a.audit("UPDATE", "SERVICE", id, null);
    return scoped("services", id);
  }

  public Object staff() {
    a.permission("staff.read");
    a.user();
    if (a.stylist())
      return db.list("select * from staff where id=? and branch_id=?", a.staffId(), a.branch());
    var rows = db.list("select * from staff where branch_id=? order by id", a.branch());
    for (var row : rows)
      row.put(
          "service_ids",
          db.jdbc.queryForList(
              "select service_id from staff_services where staff_id=?", Long.class, row.get("id")));
    return rows;
  }

  @Transactional
  public Object staff(@Valid Staff s) {
    a.permission("staff.write");
    return saveStaff(null, s);
  }

  @Transactional
  public Object staffEdit(long id, @Valid Staff s) {
    a.permission("staff.write");
    scoped("staff", id);
    return saveStaff(id, s);
  }

  public Object saveStaff(Long id, Staff s) {
    if (!s.workEnd().isAfter(s.workStart())) throw ApiError.bad("بازه ساعت کاری معتبر نیست");
    if (id == null)
      id =
          db.id("insert into staff(branch_id,name) values(?,?) returning id", a.branch(), s.name());
    db.run(
        "update staff set"
            + " name=?,mobile=?,specialty=?,commission_rate=?,work_start=?,work_end=?,work_days=?,active=?"
            + " where id=?",
        s.name(),
        Objects.requireNonNullElse(s.mobile(), ""),
        Objects.requireNonNullElse(s.specialty(), ""),
        s.commissionRate(),
        s.workStart(),
        s.workEnd(),
        s.workDays(),
        s.active(),
        id);
    db.run("delete from staff_services where staff_id=?", id);
    for (long serviceId : new HashSet<>(s.serviceIds())) {
      scoped("services", serviceId);
      db.run("insert into staff_services(staff_id,service_id) values(?,?)", id, serviceId);
    }
    a.audit("SAVE", "STAFF", id, null);
    return scoped("staff", id);
  }

  public Object leaves(long id) {
    a.permission("staff.read");
    scoped("staff", id);
    return db.list("select * from staff_leave where staff_id=? order by starts_at desc", id);
  }

  @Transactional
  public Object leave(long id, @Valid Leave l) {
    a.permission("staff.write");
    db.one("select * from staff where id=? and branch_id=? for update", id, a.branch());
    if (!l.endsAt().isAfter(l.startsAt())) throw ApiError.bad("بازه مرخصی معتبر نیست");
    if (db.id(
            "select count(*) from appointment_items where staff_id=? and active and starts_at<? and"
                + " blocked_until>?",
            id,
            l.endsAt(),
            l.startsAt())
        > 0) throw ApiError.conflict("در این بازه نوبت فعال وجود دارد");
    return Map.of(
        "id",
        db.id(
            "insert into staff_leave(staff_id,starts_at,ends_at,reason) values(?,?,?,?) returning"
                + " id",
            id,
            l.startsAt(),
            l.endsAt(),
            l.reason()));
  }

  public Object products() {
    a.permission("products.read");
    return db.list("select * from products where branch_id=? order by id", a.branch());
  }

  public Object product(@Valid Product p) {
    a.permission("products.write");
    return Map.of(
        "id",
        db.id(
            "insert into products(branch_id,name,category,price,cost,minimum_stock,supplier,active)"
                + " values(?,?,?,?,?,?,?,?) returning id",
            a.branch(),
            p.name(),
            p.category(),
            p.price(),
            p.cost(),
            p.minimumStock(),
            p.supplier(),
            p.active()));
  }

  public Object productEdit(long id, @Valid Product p) {
    a.permission("products.write");
    scoped("products", id);
    db.run(
        "update products set name=?,category=?,price=?,cost=?,minimum_stock=?,supplier=?,active=?"
            + " where id=?",
        p.name(),
        p.category(),
        p.price(),
        p.cost(),
        p.minimumStock(),
        p.supplier(),
        p.active(),
        id);
    return scoped("products", id);
  }

  @Transactional
  public Object stock(long id, @Valid Stock p) {
    a.permission("products.write");
    if (p.quantity() == 0) throw ApiError.bad("مقدار نباید صفر باشد");
    var row =
        db.one("select * from products where id=? and branch_id=? for update", id, a.branch());
    if (Db.number(row, "stock") + p.quantity() < 0) throw ApiError.bad("موجودی کافی نیست");
    db.run("update products set stock=stock+? where id=?", p.quantity(), id);
    db.run(
        "insert into stock_movements(product_id,quantity,reason) values(?,?,?)",
        id,
        p.quantity(),
        p.reason());
    a.audit("STOCK", "PRODUCT", id, p.reason());
    return scoped("products", id);
  }

  public Object users() {
    return iam.users();
  }

  @Transactional
  public Object user(@Valid User u) {
    return iam.saveUser(null,new AccessManagementService.UserWrite(u.name(),u.username(),u.password(),iam.builtInRole(u.role()),u.branchId(),u.staffId(),null,true,Map.of(),0L));
  }

  public Object userPassword(long id, UserPassword body) {
    return iam.reset(id,new AccessManagementService.Password(body.password()));
  }

  public Object deactivate(long id) {
    var row=db.one("select * from app_users where id=?",id);
    return iam.saveUser(id,iam.legacyUpdate(id,row.get("name").toString(),row.get("username").toString(),row.get("role").toString(),Db.number(row,"branch_id"),row.get("staff_id")==null?null:Db.number(row,"staff_id"),false));
  }

  public Object branches() {
    a.roles("ADMIN");
    return db.list("select * from branches order by id");
  }

  @Transactional
  public Object branch(@Valid Name n) {
    a.roles("ADMIN");
    long id = db.id("insert into branches(name,salon_id) values(?,?) returning id", n.name(),a.salon());
    db.run("insert into accounts(branch_id,name) values(?,'صندوق اصلی')", id);
    return Map.of("id", id);
  }

  @Transactional
  public Object settings(@Valid Settings s) {
    a.roles("ADMIN");
    if (s.expiresOn()==null) throw ApiError.bad("تاریخ پایان اشتراک الزامی است");
    db.run(
        "update salons set"
            + " name=?,plan=?,expires_on=?,point_unit=?,risk_days=?,loyal_visits=? where"
            + " id=?",
        s.organization(),
        s.plan(),
        s.expiresOn(),
        s.pointUnit(),
        s.riskDays(),
        s.loyalVisits(),a.salon());
    a.audit("UPDATE", "SETTINGS", 1, null);
    return a.settings();
  }

  public Object audit() {
    a.permission("audit.read");
    return db.list(
        "select l.*,u.name user_name from audit_logs l left join app_users u on u.id=l.user_id"
            + " where l.branch_id=? order by l.id desc limit 200",
        a.branch());
  }
}
