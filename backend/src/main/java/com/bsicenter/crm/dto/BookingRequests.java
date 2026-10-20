package com.bsicenter.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public final class BookingRequests {
  private BookingRequests() {}

  public record Item(@Positive long serviceId, @Positive long staffId, @Positive Long resourceId) {}

  public record Create(
      @Positive long customerId,
      @NotNull OffsetDateTime startsAt,
      @NotEmpty @Size(max = 10) List<@NotNull @Valid Item> items,
      @Size(max = 1000) String notes,
      @NotNull @Pattern(regexp = "RECEPTION|PHONE|ONLINE|OTHER") String source) {}

  public record Reschedule(
      @NotNull @Min(0) Long version,
      @NotNull OffsetDateTime startsAt,
      @NotEmpty @Size(max = 10) List<@NotNull @Valid Item> items,
      @NotBlank @Size(max = 500) String reason) {}

  public record Availability(
      @NotNull LocalDate date,
      @NotEmpty @Size(max = 10) List<@NotNull @Valid Item> items,
      Long excludeAppointmentId) {}
}
