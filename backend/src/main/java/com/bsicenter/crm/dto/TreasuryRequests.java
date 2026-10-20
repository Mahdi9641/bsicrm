package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class TreasuryRequests {
  private TreasuryRequests() {}

  public record Transfer(
      @Positive long fromAccountId,
      @Positive long toAccountId,
      @NotNull @DecimalMin("1") @DecimalMax("9999999999999999") BigDecimal amount,
      @NotBlank @Size(max = 300) String reason,
      @NotNull UUID idempotencyKey) {}

  public record Account(
      @NotBlank @Size(max = 120) String name,
      @NotNull @DecimalMin("0") @DecimalMax("9999999999999999") BigDecimal openingBalance) {}
}
