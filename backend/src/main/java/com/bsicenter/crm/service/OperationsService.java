package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.OperationsRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class OperationsService {
  private final Db db;
  private final Access a;
  private final BookingService booking;

  public OperationsService(Db db, Access a, BookingService booking) {
    this.db = db;
    this.a = a;
    this.booking = booking;
  }

  public Object appointments(OffsetDateTime from, OffsetDateTime to) {
    a.roles("ADMIN", "MANAGER", "RECEPTION", "STYLIST");
    if (!to.isAfter(from) || Duration.between(from, to).toDays() > 93)
      throw ApiError.bad("بازه گزارش حداکثر ۹۳ روز است");
    String filter =
        a.stylist()
            ? " and exists(select 1 from appointment_items ai where ai.appointment_id=p.id and"
                + " ai.staff_id="
                + a.staffId()
                + ")"
            : "";
    return db.list(
        "select p.*,c.first_name||' '||c.last_name customer_name,(select string_agg(s.name,'، ')"
            + " from appointment_items x join services s on s.id=x.service_id where"
            + " x.appointment_id=p.id) service_names,(select string_agg(distinct s.name,'، ') from"
            + " appointment_items x join staff s on s.id=x.staff_id where x.appointment_id=p.id)"
            + " staff_names from appointments p join customers c on c.id=p.customer_id where"
            + " p.branch_id=? and p.starts_at>=? and p.starts_at<?"
            + filter
            + " order by p.starts_at",
        a.branch(),
        from,
        to);
  }

  public Object book(@Valid com.bsicenter.crm.dto.BookingRequests.Create body) {
    return booking.create(body);
  }

  public Object appointment(long id) {
    return booking.detail(id);
  }

  public Object availability(@Valid com.bsicenter.crm.dto.BookingRequests.Availability body) {
    return booking.availability(body);
  }

  public Object reschedule(long id, @Valid com.bsicenter.crm.dto.BookingRequests.Reschedule body) {
    return booking.reschedule(id, body);
  }

  @Transactional
  public Object status(long id, @Valid Status s) {
    a.operations();
    var ap =
        db.one("select * from appointments where id=? and branch_id=? for update", id, a.branch());
    String old = ap.get("status").toString();
    boolean valid =
        (old.equals("BOOKED") && s.status().equals("CONFIRMED"))
            || (Set.of("BOOKED", "CONFIRMED").contains(old)
                && Set.of("CANCELLED", "NO_SHOW").contains(s.status()));
    if (!valid) throw ApiError.bad("تغییر وضعیت مجاز نیست؛ ورود و تکمیل از بخش پذیرش انجام می‌شود");
    db.run(
        "update appointments set status=?,cancel_reason=?,version=version+1,updated_at=now() where"
            + " id=?",
        s.status(),
        s.reason(),
        id);
    if (Set.of("CANCELLED", "NO_SHOW").contains(s.status()))
      db.run("update appointment_items set active=false where appointment_id=?", id);
    a.audit(s.status(), "APPOINTMENT", id, s.reason());
    return Map.of("ok", true);
  }

  @Transactional
  public Object checkIn(@Valid CheckIn c) {
    a.operations();
    db.one(
        "select id from customers where id=? and branch_id=? and active",
        c.customerId(),
        a.branch());
    if (c.appointmentId() != null) {
      var p =
          db.one(
              "select * from appointments where id=? and branch_id=? and customer_id=? for update",
              c.appointmentId(),
              a.branch(),
              c.customerId());
      if (!Set.of("BOOKED", "CONFIRMED").contains(p.get("status")))
        throw ApiError.bad("این نوبت قابل پذیرش نیست");
      db.run(
          "update appointments set status='ARRIVED',version=version+1,updated_at=now() where id=?",
          c.appointmentId());
    }
    long id =
        db.id(
            "insert into visits(branch_id,customer_id,appointment_id,priority,notes)"
                + " values(?,?,?,?,?) returning id",
            a.branch(),
            c.customerId(),
            c.appointmentId(),
            c.priority(),
            c.notes());
    if (c.appointmentId() != null)
      db.run(
          "insert into visit_services(visit_id,service_id,staff_id) select ?,service_id,staff_id"
              + " from appointment_items where appointment_id=?",
          id,
          c.appointmentId());
    a.audit("CHECK_IN", "VISIT", id, null);
    return Map.of("id", id);
  }

  public Object visits() {
    a.roles("ADMIN", "MANAGER", "RECEPTION", "STYLIST");
    String filter =
        a.stylist()
            ? " and exists(select 1 from visit_services vs where vs.visit_id=v.id and vs.staff_id="
                + a.staffId()
                + ")"
            : "";
    var result =
        db.list(
            "select v.*,c.first_name||' '||c.last_name customer_name,(select id from invoices where"
                + " visit_id=v.id) invoice_id from visits v join customers c on c.id=v.customer_id"
                + " where v.branch_id=?"
                + filter
                + " order by v.id desc limit 100",
            a.branch());
    for (var v : result) {
      v.put(
          "items",
          db.list(
              "select x.*,s.name service_name,t.name staff_name,s.price from visit_services x join"
                  + " services s on s.id=x.service_id join staff t on t.id=x.staff_id where"
                  + " x.visit_id=?"
                  + (a.stylist() ? " and x.staff_id=" + a.staffId() : ""),
              v.get("id")));
    }
    return result;
  }

  @Transactional
  public Object addService(long id, @Valid BookingItem b) {
    a.operations();
    db.one(
        "select * from visits where id=? and branch_id=? and status in ('WAITING','IN_SERVICE') for"
            + " update",
        id,
        a.branch());
    db.one(
        "select id from services where id=? and branch_id=? and active", b.serviceId(), a.branch());
    db.one(
        "select s.id from staff s join staff_services x on x.staff_id=s.id where s.id=? and"
            + " s.branch_id=? and x.service_id=? and s.active",
        b.staffId(),
        a.branch(),
        b.serviceId());
    return Map.of(
        "id",
        db.id(
            "insert into visit_services(visit_id,service_id,staff_id) values(?,?,?) returning id",
            id,
            b.serviceId(),
            b.staffId()));
  }

  @Transactional
  public Object serviceStatus(long id, @Valid ServiceStatus s) {
    a.roles("ADMIN", "MANAGER", "RECEPTION", "STYLIST");
    var row =
        db.one(
            "select x.*,v.branch_id from visit_services x join visits v on v.id=x.visit_id where"
                + " x.id=? and v.branch_id=? for update of x",
            id,
            a.branch());
    if (a.stylist() && Db.number(row, "staff_id") != a.staffId()) throw ApiError.missing();
    String old = row.get("status").toString();
    if (!(old.equals("PLANNED") && Set.of("IN_SERVICE", "CANCELLED").contains(s.status())
        || old.equals("IN_SERVICE") && Set.of("COMPLETED", "CANCELLED").contains(s.status())))
      throw ApiError.bad("ترتیب وضعیت خدمت معتبر نیست");
    db.run(
        "update visit_services set status=?,started_at=case when ?='IN_SERVICE' then now() else"
            + " started_at end,completed_at=case when ?='COMPLETED' then now() else null"
            + " end,rating=? where id=?",
        s.status(),
        s.status(),
        s.status(),
        s.rating(),
        id);
    db.run(
        "update visits set status='IN_SERVICE' where id=? and status='WAITING'",
        row.get("visit_id"));
    return Map.of("ok", true);
  }

  @Transactional
  public Object complete(long id) {
    a.operations();
    var v =
        db.one(
            "select * from visits where id=? and branch_id=? and status in ('WAITING','IN_SERVICE')"
                + " for update",
            id,
            a.branch());
    if (db.id(
                "select count(*) from visit_services where visit_id=? and status in"
                    + " ('PLANNED','IN_SERVICE')",
                id)
            > 0
        || db.id("select count(*) from visit_services where visit_id=? and status='COMPLETED'", id)
            == 0) throw ApiError.bad("ابتدا خدمات مراجعه را تکمیل کنید");
    db.run("update visits set status='COMPLETED',completed_at=now() where id=?", id);
    if (v.get("appointment_id") != null)
      db.run(
          "update appointments set status='COMPLETED',version=version+1,updated_at=now() where"
              + " id=?",
          v.get("appointment_id"));
    a.audit("COMPLETE", "VISIT", id, null);
    return Map.of("ok", true);
  }
}
