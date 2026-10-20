package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.ReportsRequests.*;
import com.bsicenter.crm.service.ReportsService;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportsController {
  private final ReportsService service;

  public ReportsController(ReportsService service) {
    this.service = service;
  }

  @GetMapping("/dashboard")
  public Object dashboard() {
    return service.dashboard();
  }

  @GetMapping("/rfm")
  public Object rfm() {
    return service.rfm();
  }

  @PostMapping("/rfm/snapshot")
  public Object snapshot() {
    return service.snapshot();
  }

  @GetMapping("/loyalty")
  public Object loyalty() {
    return service.loyalty();
  }

  @PostMapping("/loyalty/{id}/points")
  public Object points(@PathVariable long id, @jakarta.validation.Valid @RequestBody Points p) {
    return service.points(id, p);
  }

  @GetMapping("/reports")
  public Object reports(
      @RequestParam OffsetDateTime from,
      @RequestParam OffsetDateTime to,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long serviceId,
      @RequestParam(required = false) Long customerId) {
    return service.reports(from, to, staffId, serviceId, customerId);
  }
}
