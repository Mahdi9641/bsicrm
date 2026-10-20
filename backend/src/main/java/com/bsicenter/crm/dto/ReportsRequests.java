package com.bsicenter.crm.dto;

import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class ReportsRequests {
  private ReportsRequests() {}

  public record Points(
      @jakarta.validation.constraints.Min(-100000) @jakarta.validation.constraints.Max(100000)
          int points,
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 300)
          String reason) {}
}
