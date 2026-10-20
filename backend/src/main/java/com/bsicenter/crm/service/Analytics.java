package com.bsicenter.crm.service;

import com.bsicenter.crm.repository.Db;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class Analytics {
  private final Db db;

  public Analytics(Db db) {
    this.db = db;
  }

  public List<Map<String, Object>> rfm(long branch) {
    var s = db.one("select s.* from salons s join branches b on b.salon_id=s.id where b.id=?",branch);
    var rows =
        db.list(
            "select c.id,c.first_name||' '||c.last_name"
                + " name,c.mobile,c.marketing_consent,c.created_at,(select count(*) from visits v"
                + " where v.customer_id=c.id and v.status='COMPLETED' and"
                + " v.check_in_at>=now()-interval '365 days') frequency,coalesce((select sum(total)"
                + " from invoices i where i.customer_id=c.id and status<>'VOID' and"
                + " created_at>=now()-interval '365 days'),0) monetary,(select max(check_in_at)"
                + " from visits v where v.customer_id=c.id and status='COMPLETED')"
                + " last_visit,coalesce((select sum(points) from loyalty_entries l where"
                + " l.customer_id=c.id),0) points from customers c where c.branch_id=? and c.active"
                + " order by c.id desc",
            branch);
    for (var row : rows) {
      Long recency =
          row.get("last_visit") == null
              ? null
              : Duration.between(
                      ((java.sql.Timestamp) row.get("last_visit")).toInstant(), Instant.now())
                  .toDays();
      long age =
          Duration.between(((java.sql.Timestamp) row.get("created_at")).toInstant(), Instant.now())
              .toDays();
      long f = Db.number(row, "frequency"), risk = Db.number(s, "risk_days");
      long elapsed = recency != null ? recency : age;
      String segment =
          elapsed > risk * 2
              ? "LOST"
              : elapsed > risk
                  ? "AT_RISK"
                  : f >= Db.number(s, "loyal_visits") ? "LOYAL" : "GROWING";
      row.put("r_score", Math.max(1, 5 - Math.min(4, elapsed / Math.max(1, risk / 4))));
      row.put("f_score", Math.min(5, 1 + f * 4 / Math.max(1, Db.number(s, "loyal_visits"))));
      row.put(
          "m_score",
          Db.money(row, "monetary")
                  .divide(Db.money(s, "point_unit"), 0, java.math.RoundingMode.DOWN)
                  .divideToIntegralValue(java.math.BigDecimal.TEN)
                  .min(java.math.BigDecimal.valueOf(4))
                  .intValue()
              + 1);
      row.put("recency", recency);
      row.put("segment", segment);
      row.put(
          "tier",
          Db.number(row, "points") >= 100
              ? "GOLD"
              : Db.number(row, "points") >= 30 ? "SILVER" : "BRONZE");
    }
    return rows;
  }
}
