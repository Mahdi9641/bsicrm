package com.bsicenter.crm.dto;

import jakarta.validation.constraints.*;

public final class ResourceRequests {
  private ResourceRequests() {}

  public record Write(
      @NotBlank @Size(max = 120) String name,
      @NotNull @Pattern(regexp = "ROOM|CHAIR|EQUIPMENT") String kind,
      boolean active) {}

  public record View(long id, String name, String kind, boolean active) {}
}
