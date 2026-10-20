package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.TreasuryRequests.*;
import com.bsicenter.crm.service.TreasuryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TreasuryController {
  private final TreasuryService service;

  public TreasuryController(TreasuryService service) {
    this.service = service;
  }

  @PostMapping("/accounts")
  public Object account(@Valid @RequestBody Account body) {
    return service.account(body);
  }

  @GetMapping("/transfers")
  public Object transfers() {
    return service.transfers();
  }

  @PostMapping("/transfers")
  public Object transfer(@Valid @RequestBody Transfer body) {
    return service.transfer(body);
  }
}
