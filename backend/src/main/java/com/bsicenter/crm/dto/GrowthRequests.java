package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;

public final class GrowthRequests {
  private GrowthRequests() {}

  public record Attribution(@Positive long invoiceId, @NotBlank @Size(max = 500) String evidence) {}

  public record Cost(@Positive long expenseId) {}
}
