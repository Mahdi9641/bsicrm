package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.NotificationRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final Db db;
  private final Access access;
  private final FinanceService finance;
  private final String publicUrl, callbackSecret;

  public NotificationService(
      Db db,
      Access access,
      FinanceService finance,
      @Value("${app.public-url}") String publicUrl,
      @Value("${app.sms-callback-secret}") String callbackSecret) {
    this.db = db;
    this.access = access;
    this.finance = finance;
    this.publicUrl = publicUrl.replaceAll("/$", "");
    this.callbackSecret = callbackSecret;
  }

  private void authorize() {
    access.permission("messages.read");
    access.plan("STANDARD");
  }

  @Transactional
  public Object templates() {
    authorize();
    var defaults =
        Map.of(
            "BOOKING",
            "{name} عزیز، نوبت شما در {organization} برای {date} ثبت شد.",
            "REMINDER",
            "{name} عزیز، یادآوری نوبت شما: {date} در {organization}.",
            "BIRTHDAY",
            "{name} عزیز، تولدتان مبارک. با آرزوی روزهای زیبا از طرف {organization}.",
            "FOLLOWUP",
            "{name} عزیز، از حضور شما در {organization} سپاسگزاریم. برای پیگیری تجربه خود با سالن"
                + " تماس بگیرید.");
    defaults.forEach(
        (kind, body) ->
            db.run(
                "insert into message_templates(branch_id,kind,body) values(?,?,?) on"
                    + " conflict(branch_id,kind) do nothing",
                access.branch(),
                kind,
                body));
    return db.list(
        "select * from message_templates where branch_id=? order by id", access.branch());
  }

  @Transactional
  public Object update(long id, Template r) {
    access.permission("messages.write");
    authorize();
    var t =
        db.one(
            "select * from message_templates where id=? and branch_id=? for update",
            id,
            access.branch());
    TaskService.verifyVersion(Db.number(t, "version"), r.version());
    String unknown =
        r.body().replace("{name}", "").replace("{date}", "").replace("{organization}", "");
    if (unknown.contains("{") || unknown.contains("}"))
      throw ApiError.bad("متغیر قالب نامعتبر است");
    db.run(
        "update message_templates set body=?,active=?,delay_hours=?,version=version+1 where id=?",
        r.body(),
        r.active(),
        r.delayHours(),
        id);
    access.audit("UPDATE", "MESSAGE_TEMPLATE", id, null);
    return Map.of("ok", true);
  }

  @Transactional
  public Object invoice(long id, InvoiceSms request) {
    access.permission("invoices.write");
    access.plan("STANDARD");
    var i =
        db.one(
            "select * from invoices where id=? and branch_id=? and status<>'VOID' for update",
            id,
            access.branch());
    String key = "invoice:" + id + ":" + request.idempotencyKey();
    var existing = db.list("select id from messages where dedupe_key=?", key);
    if (!existing.isEmpty()) return existing.get(0);
    var link = (Map<?, ?>) finance.invoiceLink(id);
    long message =
        db.id(
            "insert into messages(branch_id,customer_id,kind,body,invoice_id,dedupe_key)"
                + " values(?,?,'INVOICE',?,?,?) returning id",
            access.branch(),
            i.get("customer_id"),
            "فاکتور شما: " + publicUrl + link.get("path"),
            id,
            key);
    access.audit("QUEUE_SMS", "INVOICE", id, null);
    return Map.of("id", message);
  }

  @Transactional
  public Object cancel(long id) {
    access.permission("messages.write");
    authorize();
    var m =
        db.one("select * from messages where id=? and branch_id=? for update", id, access.branch());
    if (!m.get("status").equals("QUEUED")) throw ApiError.conflict("فقط پیام در صف قابل لغو است");
    db.run("update messages set status='CANCELLED' where id=?", id);
    access.audit("CANCEL", "MESSAGE", id, null);
    return Map.of("ok", true);
  }

  @Transactional
  public Object delivery(String secret, Delivery r) {
    if (callbackSecret.length() < 32
        || secret == null
        || !MessageDigest.isEqual(
            callbackSecret.getBytes(StandardCharsets.UTF_8),
            secret.getBytes(StandardCharsets.UTF_8)))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN, "احراز سرویس پیامک نامعتبر است");
    var m =
        db.one(
            "select id,delivery_status from messages where id=? and provider_id=? and status='SENT'"
                + " for update",
            r.messageId(),
            r.providerId());
    if (!m.get("delivery_status").equals("UNKNOWN") && !m.get("delivery_status").equals(r.status()))
      throw ApiError.conflict("وضعیت نهایی تحویل قبلاً ثبت شده است");
    db.run(
        "update messages set delivery_status=?,delivered_at=coalesce(delivered_at,now()) where"
            + " id=?",
        r.status(),
        r.messageId());
    return Map.of("ok", true);
  }

  public static String render(String body, String name, String date, String organization) {
    return body.replace("{name}", name)
        .replace("{date}", date)
        .replace("{organization}", organization);
  }

  @Transactional
  public void generate() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Tehran"));
    // Lock templates to serialize jobs across replicas; unique dedupe keys protect replay.
    for (var template :
        db.list(
            "select * from message_templates where active order by id for update skip locked")) {
      String kind = template.get("kind").toString();
      long branch = Db.number(template, "branch_id"), delay = Db.number(template, "delay_hours");
      var settings=db.one("select s.*,s.name organization,b.active branch_active from salons s join branches b on b.salon_id=s.id where b.id=?",branch);
      if (!Boolean.TRUE.equals(settings.get("active")) || !Boolean.TRUE.equals(settings.get("branch_active")) || settings.get("plan").equals("BASIC") || ((java.sql.Date)settings.get("expires_on")).toLocalDate().isBefore(today)) continue;
      String sql =
          switch (kind) {
            case "BOOKING" ->
                "select c.id customer_id,c.first_name name,p.id source_id,p.version,p.starts_at"
                    + " event_at from appointments p join customers c on c.id=p.customer_id where"
                    + " p.branch_id=? and c.active and p.status in ('BOOKED','CONFIRMED') and"
                    + " p.starts_at>now() and p.updated_at>now()-interval '24 hours'";
            case "REMINDER" ->
                "select c.id customer_id,c.first_name name,p.id source_id,p.version,p.starts_at"
                    + " event_at from appointments p join customers c on c.id=p.customer_id where"
                    + " p.branch_id=? and c.active and p.status in ('BOOKED','CONFIRMED') and"
                    + " p.starts_at>now() and p.starts_at<=now()+interval '1 hour'*"
                    + delay;
            case "BIRTHDAY" ->
                "select c.id customer_id,c.first_name name,c.id source_id,0 version,now() event_at"
                    + " from customers c where c.branch_id=? and c.active and c.marketing_consent"
                    + " and to_char(c.birth_date,'MM-DD')=to_char(now() at time zone"
                    + " 'Asia/Tehran','MM-DD')";
            default ->
                "select c.id customer_id,c.first_name name,v.id source_id,0 version,v.completed_at"
                    + " event_at from visits v join customers c on c.id=v.customer_id where"
                    + " v.branch_id=? and c.active and c.marketing_consent and v.status='COMPLETED'"
                    + " and v.completed_at<=now()-interval '1 hour'*"
                    + delay
                    + " and v.completed_at>now()-interval '1 hour'*"
                    + (delay + 24);
          };
      for (var r : db.list(sql, branch)) {
        boolean appointment = Set.of("BOOKING", "REMINDER").contains(kind);
        String key =
            kind
                + ":"
                + r.get("source_id")
                + ":"
                + (kind.equals("BIRTHDAY") ? today.toString() : r.get("version"));
        String date =
            ((java.sql.Timestamp) r.get("event_at"))
                .toInstant()
                .atZone(ZoneId.of("Asia/Tehran"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        String body =
            render(
                template.get("body").toString(),
                r.get("name").toString(),
                date,
                settings.get("organization").toString());
        if (body.length() > 1000) continue;
        db.run(
            "insert into"
                + " messages(branch_id,customer_id,kind,body,dedupe_key,appointment_id,appointment_version)"
                + " values(?,?,?,?,?,?,?) on conflict(dedupe_key) where dedupe_key is not null do"
                + " nothing",
            branch,
            r.get("customer_id"),
            kind,
            body,
            key,
            appointment ? r.get("source_id") : null,
            appointment ? r.get("version") : null);
      }
    }
  }
}
