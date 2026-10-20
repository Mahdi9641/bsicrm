package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class EngagementRequests {
  private EngagementRequests() {}

  public record Campaign(
      @NotBlank @Size(max = 160) String name,
      @Size(max = 300) String objective,
      @Pattern(regexp = "ALL|LOYAL|GROWING|NEW|AT_RISK|LOST") @NotNull String segment,
      @NotBlank @Size(max = 1000) String body,
      @NotNull @DecimalMin("0") BigDecimal budget) {}

  public record Event(
      @NotBlank @Size(max = 160) String name,
      @Size(max = 1000) String description,
      @NotNull OffsetDateTime startsAt,
      @NotNull OffsetDateTime endsAt,
      @NotBlank @Size(max = 250) String venue,
      @Min(1) @Max(100000) int capacity,
      @NotNull @DecimalMin("0") BigDecimal budget) {}

  public record Participant(
      @Positive long customerId,
      @Pattern(regexp = "REGISTERED|ATTENDED|CANCELLED") @NotNull String status) {}
}
