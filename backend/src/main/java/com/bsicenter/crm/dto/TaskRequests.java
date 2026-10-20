package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class TaskRequests {
  private TaskRequests() {}

  public record Write(
      @NotBlank @Size(max = 160) String title,
      @Size(max = 2000) String description,
      Long customerId,
      @Positive long assignedTo,
      @NotNull OffsetDateTime dueAt,
      @NotNull @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority,
      Long version) {}

  public record Transition(
      @NotNull @Min(0) Long version,
      @NotNull @Pattern(regexp = "OPEN|IN_PROGRESS|DONE|CANCELLED") String status) {}

  public record Comment(@NotBlank @Size(max = 2000) String body) {}
}
