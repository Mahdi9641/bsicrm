package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.EngagementRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class EngagementService {
  private final Db db;
  private final Access a;
  private final Analytics analytics;

  public EngagementService(Db db, Access a, Analytics analytics) {
    this.db = db;
    this.a = a;
    this.analytics = analytics;
  }

  public Object campaigns() {
    a.marketing();
    a.plan("ADVANCED");
    return db.list(
        "select c.*,(select count(*) from campaign_recipients r where r.campaign_id=c.id)"
            + " recipients,(select count(*) from messages m where m.campaign_id=c.id and"
            + " m.status='SENT') sent from campaigns c where branch_id=? order by id desc",
        a.branch());
  }

  public Object campaign(@Valid Campaign c) {
    a.marketing();
    a.plan("ADVANCED");
    return Map.of(
        "id",
        db.id(
            "insert into campaigns(branch_id,name,objective,segment,body,budget)"
                + " values(?,?,?,?,?,?) returning id",
            a.branch(),
            c.name(),
            c.objective(),
            c.segment(),
            c.body(),
            FinanceService.amount(c.budget())));
  }

  @Transactional
  public Object queue(long id) {
    a.marketing();
    a.plan("ADVANCED");
    var c = db.one("select * from campaigns where id=? and branch_id=? for update", id, a.branch());
    if (!c.get("status").equals("DRAFT")) throw ApiError.bad("فقط پیش‌نویس قابل آماده‌سازی است");
    int count = 0;
    for (var customer : analytics.rfm(a.branch())) {
      if (!Boolean.TRUE.equals(customer.get("marketing_consent"))
          || !(c.get("segment").equals("ALL") || c.get("segment").equals(customer.get("segment"))))
        continue;
      long message =
          db.id(
              "insert into messages(branch_id,customer_id,campaign_id,body) values(?,?,?,?)"
                  + " returning id",
              a.branch(),
              customer.get("id"),
              id,
              c.get("body"));
      db.run(
          "insert into campaign_recipients(campaign_id,customer_id,message_id) values(?,?,?)",
          id,
          customer.get("id"),
          message);
      count++;
    }
    db.run("update campaigns set status='QUEUED' where id=?", id);
    a.audit("QUEUE", "CAMPAIGN", id, "recipients=" + count);
    return Map.of("count", count);
  }

  @Transactional
  public Object cancel(long id) {
    a.marketing();
    a.plan("ADVANCED");
    db.one("select id from campaigns where id=? and branch_id=? for update", id, a.branch());
    db.run("update campaigns set status='CANCELLED' where id=?", id);
    db.run("update messages set status='CANCELLED' where campaign_id=? and status='QUEUED'", id);
    return Map.of("ok", true);
  }

  public Object messages() {
    a.marketing();
    a.plan("STANDARD");
    return db.list(
        "select m.*,c.first_name||' '||c.last_name customer_name from messages m join customers c"
            + " on c.id=m.customer_id where m.branch_id=? order by m.id desc limit 200",
        a.branch());
  }

  public Object events() {
    a.marketing();
    a.plan("ADVANCED");
    return db.list(
        "select e.*,(select count(*) from event_participants x where x.event_id=e.id and"
            + " status<>'CANCELLED') registered,(select count(*) from event_participants x where"
            + " x.event_id=e.id and status='ATTENDED') attended from events e where branch_id=?"
            + " order by starts_at desc",
        a.branch());
  }

  public Object event(@Valid Event e) {
    a.marketing();
    a.plan("ADVANCED");
    if (!e.endsAt().isAfter(e.startsAt())) throw ApiError.bad("زمان پایان معتبر نیست");
    return Map.of(
        "id",
        db.id(
            "insert into events(branch_id,name,description,starts_at,ends_at,venue,capacity,budget)"
                + " values(?,?,?,?,?,?,?,?) returning id",
            a.branch(),
            e.name(),
            e.description(),
            e.startsAt(),
            e.endsAt(),
            e.venue(),
            e.capacity(),
            FinanceService.amount(e.budget())));
  }

  public Object participants(long id) {
    a.marketing();
    a.plan("ADVANCED");
    db.one("select id from events where id=? and branch_id=?", id, a.branch());
    return db.list(
        "select x.*,c.first_name||' '||c.last_name name from event_participants x join customers c"
            + " on c.id=x.customer_id where event_id=?",
        id);
  }

  @Transactional
  public Object participant(long id, @Valid Participant p) {
    a.marketing();
    a.plan("ADVANCED");
    var e =
        db.one(
            "select * from events where id=? and branch_id=? and status='OPEN' for update",
            id,
            a.branch());
    db.one(
        "select id from customers where id=? and branch_id=? and active",
        p.customerId(),
        a.branch());
    var old =
        db.list(
            "select status from event_participants where event_id=? and customer_id=?",
            id,
            p.customerId());
    boolean wasActive = !old.isEmpty() && !old.get(0).get("status").equals("CANCELLED");
    if (!p.status().equals("CANCELLED")
        && !wasActive
        && db.id(
                "select count(*) from event_participants where event_id=? and status<>'CANCELLED'",
                id)
            >= Db.number(e, "capacity")) throw ApiError.conflict("ظرفیت رویداد تکمیل است");
    db.run(
        "insert into event_participants(event_id,customer_id,status) values(?,?,?) on"
            + " conflict(event_id,customer_id) do update set status=excluded.status",
        id,
        p.customerId(),
        p.status());
    return Map.of("ok", true);
  }
}
