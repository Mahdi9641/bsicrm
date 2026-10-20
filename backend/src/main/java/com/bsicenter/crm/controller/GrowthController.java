package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.GrowthRequests.*;
import com.bsicenter.crm.service.GrowthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/growth/{type}/{id}")
public class GrowthController {
  private final GrowthService service;

  public GrowthController(GrowthService service) {
    this.service = service;
  }

  @GetMapping
  public Object report(@PathVariable String type, @PathVariable long id) {
    return service.report(type, id);
  }

  @PostMapping("/attributions")
  public Object attribute(
      @PathVariable String type, @PathVariable long id, @Valid @RequestBody Attribution body) {
    return service.attribute(type, id, body);
  }

  @PostMapping("/costs")
  public Object cost(
      @PathVariable String type, @PathVariable long id, @Valid @RequestBody Cost body) {
    return service.cost(type, id, body);
  }
}
