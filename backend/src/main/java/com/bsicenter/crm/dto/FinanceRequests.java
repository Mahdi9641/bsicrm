package com.bsicenter.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class FinanceRequests {
  private FinanceRequests() {}

  public record Line(
      Long serviceId,
      Long productId,
      Long staffId,
      @Min(1) @Max(1000) int quantity,
      @NotNull @DecimalMin("0") BigDecimal discount) {}

  public record Invoice(
      @Positive long customerId,
      Long visitId,
      @NotEmpty @Size(max = 50) List<@Valid Line> items,
      @NotNull @DecimalMin("0") BigDecimal tax) {}

  public record Payment(
      @NotNull @DecimalMin("1") BigDecimal amount,
      @Pattern(regexp = "CASH|CARD|TRANSFER") @NotNull String method,
      @Positive long accountId,
      @Size(max = 100) String trackingCode,
      @NotNull UUID idempotencyKey) {}

  public record Reason(@NotBlank @Size(max = 500) String reason) {}

  public record Expense(
      @NotBlank @Size(max = 160) String title,
      @NotBlank @Size(max = 80) String category,
      @NotNull @DecimalMin("1") BigDecimal amount,
      @Positive long accountId) {}

  public record Settlement(
      @Pattern(regexp = "APPROVED|SETTLED") @NotNull String status, Long accountId) {}
}
