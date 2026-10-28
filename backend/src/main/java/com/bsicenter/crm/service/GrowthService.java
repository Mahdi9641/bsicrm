package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.GrowthRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrowthService {
  private final Db db;
  private final Access access;

  public GrowthService(Db db, Access access) {
    this.db = db;
    this.access = access;
  }

  private String column(String type, long id) {
    access.permission(type+".read");
    access.plan("ADVANCED");
    if (!Set.of("campaigns", "events").contains(type)) throw ApiError.missing();
    db.one("select id from " + type + " where id=? and branch_id=?", id, access.branch());
    return type.equals("campaigns") ? "campaign_id" : "event_id";
  }

  @Transactional
  public Object attribute(String type, long id, Attribution r) {
    String col = column(type, id);
    access.permission(type+".write");
    var invoice =
        db.one(
            "select * from invoices where id=? and branch_id=? and status<>'VOID' for update",
            r.invoiceId(),
            access.branch());
    String participants = type.equals("campaigns") ? "campaign_recipients" : "event_participants";
    db.one(
        "select customer_id from "
            + participants
            + " where "
            + col
            + "=? and customer_id=?"
            + (type.equals("events") ? " and status<>'CANCELLED'" : ""),
        id,
        invoice.get("customer_id"));
    long key =
        db.id(
            "insert into growth_attributions(branch_id,"
                + col
                + ",invoice_id,evidence,created_by) values(?,?,?,?,?) returning id",
            access.branch(),
            id,
            r.invoiceId(),
            r.evidence(),
            access.uid());
    access.audit("ATTRIBUTION", "INVOICE", r.invoiceId(), r.evidence());
    return Map.of("id", key);
  }

  @Transactional
  public Object cost(String type, long id, Cost r) {
    String col = column(type, id);
    access.permission("finance.write");
    db.one("select id from expenses where id=? and branch_id=?", r.expenseId(), access.branch());
    long key =
        db.id(
            "insert into growth_costs(branch_id,"
                + col
                + ",expense_id,created_by) values(?,?,?,?) returning id",
            access.branch(),
            id,
            r.expenseId(),
            access.uid());
    access.audit("ATTRIBUTE_COST", type.toUpperCase(Locale.ROOT), id, null);
    return Map.of("id", key);
  }

  @Transactional(readOnly = true)
  public Object report(String type, long id) {
    String col = column(type, id);
    var rows =
        db.list(
            "select i.id,i.customer_id,i.status,i.total,i.created_at,g.evidence,coalesce((select"
                + " sum(x.total-x.unit_cost*x.quantity) from invoice_items x where"
                + " x.invoice_id=i.id),0)-coalesce((select sum(c.amount) from commissions c join"
                + " invoice_items x on x.id=c.invoice_item_id where x.invoice_id=i.id and"
                + " c.status<>'VOID'),0) contribution,coalesce((select sum(p.amount) from payments"
                + " p where p.invoice_id=i.id and not p.reversed),0) collected from"
                + " growth_attributions g join invoices i on i.id=g.invoice_id where g.branch_id=?"
                + " and g."
                + col
                + "=? and i.status<>'VOID' order by i.id desc",
            access.branch(),
            id);
    var costs =
        db.list(
            "select e.id,e.title,e.amount,e.created_at from growth_costs g join expenses e on"
                + " e.id=g.expense_id where g.branch_id=? and g."
                + col
                + "=? order by e.id desc",
            access.branch(),
            id);
    BigDecimal revenue = BigDecimal.ZERO,
        contribution = BigDecimal.ZERO,
        paid = BigDecimal.ZERO,
        cost = BigDecimal.ZERO;
    for (var r : rows) {
      revenue = revenue.add(Db.money(r, "total"));
      contribution = contribution.add(Db.money(r, "contribution"));
      paid = paid.add(Db.money(r, "collected"));
    }
    for (var c : costs) cost = cost.add(Db.money(c, "amount"));
    long targets =
        db.id(
            "select count(*) from "
                + (type.equals("campaigns") ? "campaign_recipients" : "event_participants")
                + " where "
                + col
                + "=?"
                + (type.equals("events") ? " and status<>'CANCELLED'" : ""),
            id);
    long converted = rows.stream().map(r -> r.get("customer_id")).distinct().count();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("invoices", rows);
    result.put("costs", costs);
    result.put("revenue", revenue);
    result.put("collected", paid);
    result.put("contribution", contribution);
    result.put("actualCost", cost);
    result.put("targets", targets);
    result.put("convertedCustomers", converted);
    result.put(
        "conversionPercent",
        targets == 0
            ? null
            : BigDecimal.valueOf(converted * 100)
                .divide(BigDecimal.valueOf(targets), 2, RoundingMode.HALF_UP));
    result.put(
        "roiPercent",
        cost.signum() == 0
            ? null
            : contribution
                .subtract(cost)
                .multiply(BigDecimal.valueOf(100))
                .divide(cost, 2, RoundingMode.HALF_UP));
    return result;
  }
}
