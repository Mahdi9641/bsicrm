package com.bsicenter.crm;

import static org.junit.jupiter.api.Assertions.*;

import com.bsicenter.crm.dto.PageResult;
import com.bsicenter.crm.service.BookingService;
import com.bsicenter.crm.service.TaskService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class WorkflowRulesTest {
  @Test
  void adjacentBookingsDoNotOverlapButBuffersDo() {
    Instant start = Instant.parse("2026-10-12T10:00:00Z");
    assertFalse(
        BookingService.overlaps(
            start, start.plusSeconds(1800), start.plusSeconds(1800), start.plusSeconds(3600)));
    assertTrue(
        BookingService.overlaps(
            start, start.plusSeconds(2400), start.plusSeconds(1800), start.plusSeconds(3600)));
  }

  @Test
  void closedTaskMustBeReopenedBeforeStartingAgain() {
    assertTrue(TaskService.allowedTransition("OPEN", "IN_PROGRESS"));
    assertTrue(TaskService.allowedTransition("IN_PROGRESS", "DONE"));
    assertTrue(TaskService.allowedTransition("DONE", "OPEN"));
    assertFalse(TaskService.allowedTransition("DONE", "IN_PROGRESS"));
    assertFalse(TaskService.allowedTransition("OPEN", "OPEN"));
  }

  @Test
  void staleAndMissingVersionsAreRejected() {
    assertDoesNotThrow(() -> TaskService.verifyVersion(3, 3L));
    assertThrows(ResponseStatusException.class, () -> TaskService.verifyVersion(3, 2L));
    assertThrows(ResponseStatusException.class, () -> TaskService.verifyVersion(3, null));
  }

  @Test
  void pageBoundsPreventUnboundedQueries() {
    assertThrows(ResponseStatusException.class, () -> PageResult.validate(-1, 30));
    assertThrows(ResponseStatusException.class, () -> PageResult.validate(0, 101));
    assertDoesNotThrow(() -> PageResult.validate(0, 100));
  }
}
