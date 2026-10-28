package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.ReportsRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.time.*;
import java.util.*;

@org.springframework.stereotype.Service
public class ReportsService {
  private final Db db;
  private final Access a;
  private final Analytics analytics;
  private final TaskService tasks;

  public ReportsService(Db db, Access a, Analytics analytics, TaskService tasks) {
    this.db = db;
    this.a = a;
    this.analytics = analytics;
    this.tasks = tasks;
  }

  public Object dashboard() {
    a.user();
    var now = LocalDate.now(ZoneId.of("Asia/Tehran"));
    var start = now.atStartOfDay(ZoneId.of("Asia/Tehran")).toOffsetDateTime();
    var end = start.plusDays(1);
    var result = new LinkedHashMap<String, Object>();
    long branch = a.branch();
    String staffFilter =
        a.stylist()
            ? " and exists(select 1 from appointment_items x where x.appointment_id=p.id and"
                + " x.staff_id="
                + a.staffId()
                + ")"
            : "";
    result.put(
        "appointments",
        db.id(
            "select count(*) from appointments p where branch_id=? and starts_at>=? and starts_at<?"
                + " and status not in ('CANCELLED','NO_SHOW')"
                + staffFilter,
            branch,
            start,
            end));
    if (!a.stylist()) {
      result.put(
          "customers",
          db.id("select count(*) from customers where branch_id=? and active", branch));
      result.put(
          "waiting",
          db.id("select count(*) from visits where branch_id=? and status='WAITING'", branch));
    }
    if (a.stylist()) {
      result.put(
          "myVisits",
          db.id(
              "select count(*) from visits v where branch_id=? and check_in_at>=? and check_in_at<?"
                  + " and exists(select 1 from visit_services x where x.visit_id=v.id and"
                  + " x.staff_id=?)",
              branch,
              start,
              end,
              a.staffId()));
      result.put(
          "waiting",
          db.id(
              "select count(*) from visits v where branch_id=? and status='WAITING' and"
                  + " exists(select 1 from visit_services x where x.visit_id=v.id and"
                  + " x.staff_id=?)",
              branch,
              a.staffId()));
    }
    result.put(
        "canSchedule", a.has("appointments.read"));
    result.put(
        "schedule",
        db.list(
            "select p.*,c.first_name||' '||c.last_name customer_name,(select string_agg(s.name,'،"
                + " ') from appointment_items x join services s on s.id=x.service_id where"
                + " x.appointment_id=p.id) service_names from appointments p join customers c on"
                + " c.id=p.customer_id where p.branch_id=? and starts_at>=? and starts_at<?"
                + staffFilter
                + " order by starts_at limit 20",
            branch,
            start,
            end));
    if (a.has("finance.read")) {
      result.put(
          "revenue",
          db.jdbc.queryForObject(
              "select coalesce(sum(total),0) from invoices where branch_id=? and status<>'VOID' and"
                  + " created_at>=? and created_at<?",
              java.math.BigDecimal.class,
              branch,
              start,
              end));
      result.put(
          "outstanding",
          db.jdbc.queryForObject(
              "select coalesce(sum(i.total-coalesce((select sum(amount) from payments p where"
                  + " p.invoice_id=i.id and not p.reversed),0)),0) from invoices i where"
                  + " branch_id=? and status<>'VOID'",
              java.math.BigDecimal.class,
              branch));
      result.put(
          "trend",
          db.list(
              "select d::date as day,coalesce((select sum(total) from invoices where branch_id=?"
                  + " and status<>'VOID' and (created_at at time zone"
                  + " 'Asia/Tehran')::date=d::date),0) total from"
                  + " generate_series(?::date-6,?::date,'1 day') d",
              branch,
              now,
              now));
      result.put(
          "topServices",
          db.list(
              "select x.description name,sum(x.total) total,count(*) count from invoice_items x"
                  + " join invoices i on i.id=x.invoice_id where i.branch_id=? and i.status<>'VOID'"
                  + " and x.service_id is not null and i.created_at>=now()-interval '30 days' group"
                  + " by x.description order by total desc limit 5",
              branch));
    }
    if (a.has("rfm.read")) {
      var groups = new LinkedHashMap<String, Long>();
      for (String key : List.of("LOYAL", "GROWING", "AT_RISK", "LOST")) groups.put(key, 0L);
      analytics
          .rfm(branch)
          .forEach(
              r -> {
                String key = r.get("segment").toString();
                groups.put(key, groups.get(key) + 1);
              });
      result.put("segments", groups);
    }
    if (a.has("tasks.read"))
      result.put("followUps", tasks.summary());
    if (a.has("products.read"))
      result.put(
          "lowStock",
          db.list(
              "select id,name,stock,minimum_stock from products where branch_id=? and active and"
                  + " stock<=minimum_stock order by stock limit 5",
              branch));
    if(!a.has("appointments.read")){result.remove("appointments");result.put("schedule",List.of());}
    if(!a.has("customers.read"))result.remove("customers");
    if(!a.has("visits.read")){result.remove("waiting");result.remove("myVisits");}
    return result;
  }

  public Object rfm() {
    a.permission("rfm.read");
    a.plan("ADVANCED");
    return analytics.rfm(a.branch());
  }

  @org.springframework.transaction.annotation.Transactional
  public Object snapshot() {
    a.permission("rfm.write");
    a.plan("ADVANCED");
    var rows = analytics.rfm(a.branch());
    for (var r : rows)
      db.run(
          "insert into rfm_snapshots(customer_id,calculated_on,recency,frequency,monetary,segment)"
              + " values(?,current_date,?,?,?,?) on conflict(customer_id,calculated_on) do update"
              + " set recency=excluded.recency,frequency=excluded.frequency,monetary=excluded.monetary,segment=excluded.segment",
          r.get("id"),
          r.get("recency"),
          r.get("frequency"),
          r.get("monetary"),
          r.get("segment"));
    return Map.of("count", rows.size());
  }

  public Object loyalty() {
    a.permission("loyalty.read");
    a.plan("STANDARD");
    return analytics.rfm(a.branch()).stream()
        .map(
            r ->
                Map.of(
                    "id",
                    r.get("id"),
                    "name",
                    r.get("name"),
                    "points",
                    r.get("points"),
                    "tier",
                    r.get("tier")))
        .toList();
  }

  @org.springframework.transaction.annotation.Transactional
  public Object points(long id, @jakarta.validation.Valid Points p) {
    a.permission("loyalty.write");
    a.plan("STANDARD");
    db.one("select id from customers where id=? and branch_id=? for update", id, a.branch());
    long total =
        db.id("select coalesce(sum(points),0) from loyalty_entries where customer_id=?", id);
    if (p.points() == 0 || total + p.points() < 0) throw ApiError.bad("تغییر امتیاز معتبر نیست");
    db.run(
        "insert into loyalty_entries(customer_id,points,reason) values(?,?,?)",
        id,
        p.points(),
        p.reason());
    a.audit("POINTS", "CUSTOMER", id, p.reason());
    return Map.of("ok", true);
  }

  public Object reports(
      OffsetDateTime from, OffsetDateTime to, Long staffId, Long serviceId, Long customerId) {
    a.permission("reports.read");
    a.plan("ADVANCED");
    if (!to.isAfter(from) || Duration.between(from, to).toDays() > 366)
      throw ApiError.bad("بازه گزارش حداکثر یک سال است");
    var params = new ArrayList<Object>(List.of(a.branch(), from, to));
    String where = "i.branch_id=? and i.created_at>=? and i.created_at<? and i.status<>'VOID'";
    if (staffId != null) {
      where += " and x.staff_id=?";
      params.add(staffId);
    }
    if (serviceId != null) {
      where += " and x.service_id=?";
      params.add(serviceId);
    }
    if (customerId != null) {
      where += " and i.customer_id=?";
      params.add(customerId);
    }
    String metrics =
        "sum(x.total) revenue,sum(x.unit_cost*x.quantity) material_cost,sum(coalesce(c.amount,0))"
            + " commission,sum(x.total-x.unit_cost*x.quantity-coalesce(c.amount,0)) contribution";
    return Map.of(
        "services",
        db.list(
            "select x.description name,sum(x.quantity) quantity,"
                + metrics
                + " from invoice_items x join invoices i on i.id=x.invoice_id left join commissions"
                + " c on c.invoice_item_id=x.id and c.status<>'VOID' where "
                + where
                + " group by x.description order by revenue desc",
            params.toArray()),
        "customers",
        db.list(
            "select u.first_name||' '||u.last_name name,"
                + metrics
                + " from invoice_items x join invoices i on i.id=x.invoice_id join customers u on"
                + " u.id=i.customer_id left join commissions c on c.invoice_item_id=x.id and"
                + " c.status<>'VOID' where "
                + where
                + " group by u.id order by contribution desc",
            params.toArray()),
        "note",
        "سود مشارکتی قبل از سربار، مالیات و هزینه جذب؛ RFM بر مراجعات تکمیل‌شده و خرید معتبر ۳۶۵"
            + " روز اخیر محاسبه می‌شود.");
  }
}
