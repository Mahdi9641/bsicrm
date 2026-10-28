package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.BookingRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Owns scheduling invariants for both creation and rescheduling. */
@Service
public class BookingService {
  static final ZoneId ZONE = ZoneId.of("Asia/Tehran");

  public record Segment(
      long serviceId,
      long staffId,
      Long resourceId,
      OffsetDateTime startsAt,
      OffsetDateTime endsAt,
      OffsetDateTime blockedUntil) {}

  public record Busy(long staffId, Instant start, Instant end) {}

  public record Context(
      Map<Long, Map<String, Object>> services,
      Map<Long, Map<String, Object>> staff,
      List<Busy> busy) {}

  private final Db db;
  private final Access access;

  public BookingService(Db db, Access access) {
    this.db = db;
    this.access = access;
  }

  public static boolean overlaps(Instant from, Instant to, Instant otherFrom, Instant otherTo) {
    return from.isBefore(otherTo) && to.isAfter(otherFrom);
  }

  private Context context(List<Item> items, LocalDate day, Long excluded, boolean lock) {
    long branch = access.branch();
    Map<Long, Map<String, Object>> staff = new HashMap<>(), services = new HashMap<>();
    for (long id :
        items.stream()
            .map(Item::resourceId)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList()) {
      db.one(
          "select id from salon_resources where id=? and branch_id=? and active"
              + (lock ? " for update" : ""),
          id,
          branch);
    }
    for (long id : items.stream().map(Item::staffId).distinct().sorted().toList()) {
      staff.put(
          id,
          db.one(
              "select * from staff where id=? and branch_id=? and active"
                  + (lock ? " for update" : ""),
              id,
              branch));
    }
    for (Item item : items) {
      services.computeIfAbsent(
          item.serviceId(),
          id -> db.one("select * from services where id=? and branch_id=? and active", id, branch));
      if (db.id(
              "select count(*) from staff_services where staff_id=? and service_id=?",
              item.staffId(),
              item.serviceId())
          == 0) throw ApiError.bad("خدمت انتخاب‌شده در مهارت‌های متخصص ثبت نشده است");
    }
    var from = day.atStartOfDay(ZONE).toOffsetDateTime();
    var to = from.plusDays(1);
    List<Busy> busy = new ArrayList<>();
    for (long id : staff.keySet()) {
      for (var row :
          db.list(
              "select starts_at,blocked_until ends_at from appointment_items where staff_id=? and"
                  + " active and starts_at<? and blocked_until>? and appointment_id<>? union all"
                  + " select starts_at,ends_at from staff_leave where staff_id=? and starts_at<?"
                  + " and ends_at>?",
              id,
              to,
              from,
              excluded == null ? -1 : excluded,
              id,
              to,
              from)) {
        busy.add(
            new Busy(
                id,
                ((java.sql.Timestamp) row.get("starts_at")).toInstant(),
                ((java.sql.Timestamp) row.get("ends_at")).toInstant()));
      }
    }
    for (long resource :
        items.stream().map(Item::resourceId).filter(Objects::nonNull).distinct().toList()) {
      for (var row :
          db.list(
              "select starts_at,blocked_until from appointment_items where resource_id=? and active"
                  + " and starts_at<? and blocked_until>? and appointment_id<>?",
              resource,
              to,
              from,
              excluded == null ? -1 : excluded)) {
        busy.add(
            new Busy(
                -resource,
                ((java.sql.Timestamp) row.get("starts_at")).toInstant(),
                ((java.sql.Timestamp) row.get("blocked_until")).toInstant()));
      }
    }
    return new Context(services, staff, busy);
  }

  private List<Segment> plan(List<Item> items, OffsetDateTime start, Context context) {
    if (start.isBefore(OffsetDateTime.now().minusMinutes(5)))
      throw ApiError.bad("زمان نوبت در گذشته است");
    List<Segment> result = new ArrayList<>();
    var cursor = start;
    for (Item item : items) {
      var service = context.services().get(item.serviceId());
      var staff = context.staff().get(item.staffId());
      var end = cursor.plusMinutes(Db.number(service, "duration_minutes"));
      var blocked = end.plusMinutes(Db.number(service, "buffer_minutes"));
      var local = cursor.atZoneSameInstant(ZONE);
      var until = blocked.atZoneSameInstant(ZONE);
      if (!local.toLocalDate().equals(start.atZoneSameInstant(ZONE).toLocalDate())
          || !local.toLocalDate().equals(until.toLocalDate())
          || !Arrays.asList(staff.get("work_days").toString().split(","))
              .contains("" + local.getDayOfWeek().getValue())
          || local.toLocalTime().isBefore(((java.sql.Time) staff.get("work_start")).toLocalTime())
          || until.toLocalTime().isAfter(((java.sql.Time) staff.get("work_end")).toLocalTime()))
        throw ApiError.bad("نوبت خارج از روز یا ساعت کاری متخصص است");
      for (Busy busy : context.busy())
        if ((busy.staffId() == item.staffId()
                || (item.resourceId() != null && busy.staffId() == -item.resourceId()))
            && overlaps(cursor.toInstant(), blocked.toInstant(), busy.start(), busy.end()))
          throw ApiError.conflict("متخصص یا منبع در این بازه آزاد نیست؛ ساعت دیگری انتخاب کنید");
      result.add(
          new Segment(item.serviceId(), item.staffId(), item.resourceId(), cursor, end, blocked));
      cursor = blocked; // The next service starts after the required buffer.
    }
    return result;
  }

  private void insertItems(long id, List<Segment> segments) {
    for (Segment s : segments)
      db.run(
          "insert into"
              + " appointment_items(appointment_id,service_id,staff_id,starts_at,ends_at,blocked_until,resource_id)"
              + " values(?,?,?,?,?,?,?)",
          id,
          s.serviceId(),
          s.staffId(),
          s.startsAt(),
          s.endsAt(),
          s.blockedUntil(),
          s.resourceId());
  }

  @Transactional
  public Object create(Create request) {
    if (access.customer()) {
      if (request.customerId() != access.customerId()) throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "دسترسی به این مشتری مجاز نیست");
    } else access.permission("appointments.write");
    long branch = access.branch();
    db.one(
        "select id from customers where id=? and branch_id=? and active",
        request.customerId(),
        branch);
    var context =
        context(
            request.items(), request.startsAt().atZoneSameInstant(ZONE).toLocalDate(), null, true);
    var plan = plan(request.items(), request.startsAt(), context);
    long id =
        db.id(
            "insert into"
                + " appointments(branch_id,customer_id,starts_at,ends_at,notes,source,created_by)"
                + " values(?,?,?,?,?,?,?) returning id",
            branch,
            request.customerId(),
            request.startsAt(),
            plan.get(plan.size() - 1).endsAt(),
            request.notes(),
            request.source(),
            access.uid());
    insertItems(id, plan);
    access.audit("CREATE", "APPOINTMENT", id, null);
    return Map.of("id", id, "version", 0);
  }

  @Transactional
  public Object reschedule(long id, Reschedule request) {
    if (!access.customer()) access.permission("appointments.write");
    var appointment =
        db.one(
            "select * from appointments where id=? and branch_id=? for update",
            id,
            access.branch());
    if (access.customer() && Db.number(appointment,"customer_id")!=access.customerId()) throw ApiError.missing();
    if (!Set.of("BOOKED", "CONFIRMED").contains(appointment.get("status")))
      throw ApiError.bad("فقط نوبت رزروشده یا تأییدشده قابل جابه‌جایی است");
    if (Db.number(appointment, "version") != request.version())
      throw ApiError.conflict("نوبت توسط کاربر دیگری تغییر کرده است؛ دوباره آن را باز کنید");
    var context =
        context(
            request.items(), request.startsAt().atZoneSameInstant(ZONE).toLocalDate(), id, true);
    var plan = plan(request.items(), request.startsAt(), context);
    db.run("delete from appointment_items where appointment_id=?", id);
    insertItems(id, plan);
    db.run(
        "insert into appointment_changes(appointment_id,changed_by,old_start,new_start,reason)"
            + " values(?,?,?,?,?)",
        id,
        access.uid(),
        appointment.get("starts_at"),
        request.startsAt(),
        request.reason());
    db.run(
        "update appointments set starts_at=?,ends_at=?,version=version+1,updated_at=now() where"
            + " id=?",
        request.startsAt(),
        plan.get(plan.size() - 1).endsAt(),
        id);
    access.audit("RESCHEDULE", "APPOINTMENT", id, request.reason());
    return Map.of("id", id, "version", request.version() + 1);
  }

  @Transactional(readOnly = true)
  public Object availability(Availability request) {
    if (!access.customer()) access.permission("appointments.read");
    if(access.stylist()&&request.items().stream().anyMatch(item->item.staffId()!=access.staffId()))throw ApiError.missing();
    LocalDate today = LocalDate.now(ZONE);
    if (request.date().isBefore(today) || request.date().isAfter(today.plusDays(180)))
      throw ApiError.bad("جستجوی زمان آزاد حداکثر تا ۱۸۰ روز آینده است");
    if (request.excludeAppointmentId() != null)
      db.one(
          "select id from appointments where id=? and branch_id=?",
          request.excludeAppointmentId(),
          access.branch());
    var context = context(request.items(), request.date(), request.excludeAppointmentId(), false);
    List<Map<String, Object>> slots = new ArrayList<>();
    var midnight = request.date().atStartOfDay(ZONE).toOffsetDateTime();
    for (int minute = 0; minute < 24 * 60; minute += 15) {
      var start = midnight.plusMinutes(minute);
      if (start.isBefore(OffsetDateTime.now())) continue;
      try {
        var segments = plan(request.items(), start, context);
        slots.add(Map.of("startsAt", start, "endsAt", segments.get(segments.size() - 1).endsAt()));
      } catch (ResponseStatusException e) {
        if (e.getStatusCode().value() != 400 && e.getStatusCode().value() != 409) throw e;
      }
    }
    return Map.of("date", request.date(), "slots", slots, "intervalMinutes", 15);
  }

  public Object detail(long id) {
    access.permission("appointments.read");
    if(access.stylist())db.one("select id from appointment_items where appointment_id=? and staff_id=?",id,access.staffId());
    var appointment =
        db.one(
            "select p.*,c.first_name||' '||c.last_name customer_name from appointments p join"
                + " customers c on c.id=p.customer_id where p.id=? and p.branch_id=?",
            id,
            access.branch());
    return Map.of(
        "appointment",
        appointment,
        "items",
        db.list("select * from appointment_items where appointment_id=?"+(access.stylist()?" and staff_id="+access.staffId():"")+" order by starts_at,id", id),
        "history",
        db.list(
            "select h.*,u.name actor_name from appointment_changes h join app_users u on"
                + " u.id=h.changed_by where appointment_id=? order by h.id desc",
            id));
  }
}
