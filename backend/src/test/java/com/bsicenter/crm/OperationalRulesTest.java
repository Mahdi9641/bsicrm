package com.bsicenter.crm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bsicenter.crm.dto.NotificationRequests.Delivery;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.service.*;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class OperationalRulesTest {
  @Test
  void tieredCommissionChargesOnlyExcessAtUpperRate() {
    assertEquals(
        new BigDecimal("250"),
        CommissionRuleService.calculate(
            "TIERED",
            new BigDecimal("1500"),
            1,
            new BigDecimal("10"),
            BigDecimal.ZERO,
            new BigDecimal("1000"),
            new BigDecimal("30")));
  }

  @Test
  void fixedCommissionUsesQuantity() {
    assertEquals(
        new BigDecimal("200"),
        CommissionRuleService.calculate(
            "FIXED",
            new BigDecimal("900"),
            2,
            BigDecimal.ZERO,
            new BigDecimal("100"),
            BigDecimal.ZERO,
            BigDecimal.ZERO));
  }

  @Test
  void commissionCannotExceedNetSale() {
    assertThrows(
        ResponseStatusException.class,
        () ->
            CommissionRuleService.calculate(
                "FIXED",
                new BigDecimal("90"),
                1,
                BigDecimal.ZERO,
                new BigDecimal("100"),
                BigDecimal.ZERO,
                BigDecimal.ZERO));
  }

  @Test
  void templateUsesLiteralReplacement() {
    assertEquals(
        "سلام مریم، سالن در 10:00",
        NotificationService.render(
            "سلام {name}، {organization} در {date}", "مریم", "10:00", "سالن"));
  }

  @Test
  void deliveryCallbackRejectsMissingSecretBeforeReadingDatabase() {
    Db db = mock(Db.class);
    var service =
        new NotificationService(
            db, null, null, "http://localhost:3000", "long-test-secret-that-is-at-least-32-chars");
    assertThrows(
        ResponseStatusException.class,
        () -> service.delivery(null, new Delivery(1, "provider-1", "DELIVERED")));
    verifyNoInteractions(db);
  }

  @Test
  void deliveryCallbackCannotOverwriteFinalStatus() {
    Db db = mock(Db.class);
    when(db.one(anyString(), any(), any()))
        .thenReturn(Map.of("id", 1L, "delivery_status", "DELIVERED"));
    String secret = "long-test-secret-that-is-at-least-32-chars";
    var service = new NotificationService(db, null, null, "http://localhost:3000", secret);
    assertThrows(
        ResponseStatusException.class,
        () -> service.delivery(secret, new Delivery(1, "provider-1", "UNDELIVERABLE")));
    verify(db, never()).run(anyString(), any(), any());
  }

  @Test
  void rfmPrioritizesRecencyAndUsesFourReferenceSegments() {
    Db db = mock(Db.class);
    when(db.one("select s.* from salons s join branches b on b.salon_id=s.id where b.id=?",1L))
        .thenReturn(
            Map.of("risk_days", 90L, "loyal_visits", 5L, "point_unit", new BigDecimal("100000")));
    java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
    for (long[] sample : new long[][] {{10, 5}, {90, 1}, {91, 8}, {180, 8}, {181, 10}}) {
      var row = new java.util.HashMap<String, Object>();
      var timestamp =
          java.sql.Timestamp.from(java.time.Instant.now().minusSeconds(sample[0] * 86400));
      row.put("last_visit", timestamp);
      row.put("created_at", timestamp);
      row.put("frequency", sample[1]);
      row.put("monetary", new BigDecimal("1000000"));
      row.put("points", 0L);
      rows.add(row);
    }
    // A customer without a completed visit is classified by age of their record.
    rows.get(4).put("last_visit", null);
    when(db.list(anyString(), any())).thenReturn(rows);
    var result = new Analytics(db).rfm(1L);
    assertEquals(
        java.util.List.of("LOYAL", "GROWING", "AT_RISK", "AT_RISK", "LOST"),
        result.stream().map(r -> r.get("segment")).toList());
  }
}
