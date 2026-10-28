package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.CommissionRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommissionRuleService {
  private final Db db;
  private final Access access;

  public CommissionRuleService(Db db, Access access) {
    this.db = db;
    this.access = access;
  }

  public Object list() {
    access.permission("commission-rules.read");
    access.plan("STANDARD");
    return db.list(
        "select r.*,s.name staff_name,v.name service_name from commission_rules r join staff s on"
            + " s.id=r.staff_id join services v on v.id=r.service_id where r.branch_id=? order by"
            + " r.id desc limit 300",
        access.branch());
  }

  @Transactional
  public Object create(Rule r) {
    access.permission("commission-rules.write");
    access.plan("STANDARD");
    if (r.validTo().isBefore(r.validFrom()))
      throw ApiError.bad("پایان اعتبار باید پس از شروع باشد");
    db.one(
        "select id from staff where id=? and branch_id=? for update", r.staffId(), access.branch());
    db.one("select id from services where id=? and branch_id=?", r.serviceId(), access.branch());
    long id =
        db.id(
            "insert into"
                + " commission_rules(branch_id,staff_id,service_id,name,kind,rate,fixed_amount,threshold,upper_rate,valid_from,valid_to,created_by)"
                + " values(?,?,?,?,?,?,?,?,?,?,?,?) returning id",
            access.branch(),
            r.staffId(),
            r.serviceId(),
            r.name(),
            r.kind(),
            r.rate(),
            FinanceService.amount(r.fixedAmount()),
            FinanceService.amount(r.threshold()),
            r.upperRate(),
            r.validFrom(),
            r.validTo(),
            access.uid());
    access.audit("CREATE", "COMMISSION_RULE", id, null);
    return Map.of("id", id);
  }

  @Transactional
  public Object deactivate(long id) {
    access.permission("commission-rules.write");
    access.plan("STANDARD");
    db.one(
        "select id from commission_rules where id=? and branch_id=? for update",
        id,
        access.branch());
    db.run("update commission_rules set active=false where id=?", id);
    access.audit("DEACTIVATE", "COMMISSION_RULE", id, null);
    return Map.of("ok", true);
  }

  public static BigDecimal calculate(
      String kind,
      BigDecimal basis,
      int quantity,
      BigDecimal rate,
      BigDecimal fixed,
      BigDecimal threshold,
      BigDecimal upper) {
    var result =
        switch (kind) {
          case "FIXED" -> fixed.multiply(BigDecimal.valueOf(quantity));
          case "TIERED" ->
              FinanceService.commission(basis.min(threshold), rate)
                  .add(
                      FinanceService.commission(
                          basis.subtract(threshold).max(BigDecimal.ZERO), upper));
          default -> FinanceService.commission(basis, rate);
        };
    if (result.compareTo(basis) > 0) throw ApiError.bad("کمیسیون از مبلغ خالص ردیف بیشتر است");
    return result;
  }

  public Quote quote(
      long staff, long service, BigDecimal basis, int quantity, BigDecimal fallback) {
    var rows =
        db.list(
            "select * from commission_rules where branch_id=? and staff_id=? and service_id=? and"
                + " active and (now() at time zone 'Asia/Tehran')::date between valid_from and"
                + " valid_to",
            access.branch(),
            staff,
            service);
    if (rows.isEmpty())
      return new Quote(
          null,
          fallback,
          FinanceService.commission(basis, fallback),
          "PERCENT; NET_AFTER_DISCOUNT; rate=" + fallback);
    var r = rows.get(0);
    var rate = Db.money(r, "rate");
    var kind = r.get("kind").toString();
    var amount =
        calculate(
            kind,
            basis,
            quantity,
            rate,
            Db.money(r, "fixed_amount"),
            Db.money(r, "threshold"),
            Db.money(r, "upper_rate"));
    return new Quote(
        Db.number(r, "id"),
        rate,
        amount,
        kind
            + "; NET_AFTER_DISCOUNT; rate="
            + rate
            + "; fixed="
            + r.get("fixed_amount")
            + "; threshold="
            + r.get("threshold")
            + "; upper="
            + r.get("upper_rate"));
  }
}
