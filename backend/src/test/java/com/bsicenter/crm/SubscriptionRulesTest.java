package com.bsicenter.crm;
import static org.junit.jupiter.api.Assertions.*;
import com.bsicenter.crm.security.Access;
import com.bsicenter.crm.service.BillingService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
class SubscriptionRulesTest {
 @Test void expiresAfterTheLastPaidDay(){var d=LocalDate.of(2026,10,20);assertFalse(Access.expired(d,d));assertTrue(Access.expired(d.minusDays(1),d));assertTrue(Access.expired(null,d));}
 @Test void renewalPreservesEveryRemainingDay(){var d=LocalDate.of(2026,10,20);assertEquals(d.plusDays(11),BillingService.renewalStart(d.plusDays(10),d));assertEquals(d.plusDays(1),BillingService.renewalStart(d,d));}
 @Test void expiredRenewalStartsToday(){var d=LocalDate.of(2026,10,20);assertEquals(d,BillingService.renewalStart(d.minusDays(10),d));assertEquals(d,BillingService.renewalStart(null,d));}
}
