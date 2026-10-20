package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class CommissionRequests {
  private CommissionRequests() {}

  public record Rule(
      @NotBlank @Size(max = 120) String name,
      @Positive long staffId,
      @Positive long serviceId,
      @NotNull @Pattern(regexp = "PERCENT|FIXED|TIERED") String kind,
      @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal rate,
      @NotNull @DecimalMin("0") @DecimalMax("9999999999999999") BigDecimal fixedAmount,
      @NotNull @DecimalMin("0") @DecimalMax("9999999999999999") BigDecimal threshold,
      @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal upperRate,
      @NotNull LocalDate validFrom,
      @NotNull LocalDate validTo) {}

  public record Quote(Long ruleId, BigDecimal rate, BigDecimal amount, String snapshot) {}
}
