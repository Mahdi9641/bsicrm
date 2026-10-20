package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class NotificationRequests {
  private NotificationRequests() {}

  public record Template(
      @NotBlank @Size(max = 800) String body,
      boolean active,
      @Min(1) @Max(168) int delayHours,
      @NotNull @Min(0) Long version) {}

  public record InvoiceSms(@NotNull UUID idempotencyKey) {}

  public record Delivery(
      @Positive long messageId,
      @NotBlank @Size(max = 120) String providerId,
      @NotNull @Pattern(regexp = "DELIVERED|UNDELIVERABLE") String status) {}
}
