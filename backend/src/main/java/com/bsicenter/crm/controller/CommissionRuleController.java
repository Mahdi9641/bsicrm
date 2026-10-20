package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.CommissionRequests.Rule;
import com.bsicenter.crm.service.CommissionRuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commission-rules")
public class CommissionRuleController {
  private final CommissionRuleService service;

  public CommissionRuleController(CommissionRuleService service) {
    this.service = service;
  }

  @GetMapping
  public Object list() {
    return service.list();
  }

  @PostMapping
  public Object create(@Valid @RequestBody Rule body) {
    return service.create(body);
  }

  @PostMapping("/{id}/deactivate")
  public Object deactivate(@PathVariable long id) {
    return service.deactivate(id);
  }
}
