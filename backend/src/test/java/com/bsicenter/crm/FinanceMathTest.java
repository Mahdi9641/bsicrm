package com.bsicenter.crm;

import static org.junit.jupiter.api.Assertions.*;

import com.bsicenter.crm.service.AuthService;
import com.bsicenter.crm.service.FinanceService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FinanceMathTest {
  @Test
  void commissionUsesDiscountedBasisAndRoundsToToman() {
    assertEquals(
        new BigDecimal("315000"),
        FinanceService.commission(new BigDecimal("900000"), new BigDecimal("35")));
    assertEquals(
        new BigDecimal("1"), FinanceService.commission(new BigDecimal("5"), new BigDecimal("10")));
  }

  @Test
  void fractionalMoneyIsRejected() {
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> FinanceService.amount(new BigDecimal("100.5")));
  }

  @Test
  void integerMoneyAcceptsZeroScaleRepresentations() {
    assertEquals(new BigDecimal("100"), FinanceService.amount(new BigDecimal("100.00")));
  }

  @Test
  void tokenHashIsDeterministicAndDoesNotStorePlainToken() {
    String token = AuthService.random();
    assertEquals(64, AuthService.hash(token).length());
    assertEquals(AuthService.hash(token), AuthService.hash(token));
    assertNotEquals(AuthService.hash(token), AuthService.hash(token + "x"));
  }
}
