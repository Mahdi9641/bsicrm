package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class OperationsRequests {
  private OperationsRequests() {}

  public record BookingItem(@Positive long serviceId, @Positive long staffId) {}

  public record Status(@NotBlank String status, @Size(max = 500) String reason) {}

  public record CheckIn(
      Long appointmentId,
      @Positive long customerId,
      @Pattern(regexp = "NORMAL|VIP") @NotNull String priority,
      @Size(max = 1000) String notes) {}

  public record ServiceStatus(
      @Pattern(regexp = "IN_SERVICE|COMPLETED|CANCELLED") @NotNull String status,
      @Min(1) @Max(5) Integer rating) {}
}
