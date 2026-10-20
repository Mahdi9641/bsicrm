package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.EngagementRequests.*;
import com.bsicenter.crm.service.EngagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EngagementController {
  private final EngagementService service;

  public EngagementController(EngagementService service) {
    this.service = service;
  }

  @GetMapping("/campaigns")
  public Object campaigns() {
    return service.campaigns();
  }

  @PostMapping("/campaigns")
  public Object campaign(@Valid @RequestBody Campaign c) {
    return service.campaign(c);
  }

  @PostMapping("/campaigns/{id}/queue")
  public Object queue(@PathVariable long id) {
    return service.queue(id);
  }

  @PostMapping("/campaigns/{id}/cancel")
  public Object cancel(@PathVariable long id) {
    return service.cancel(id);
  }

  @GetMapping("/messages")
  public Object messages() {
    return service.messages();
  }

  @GetMapping("/events")
  public Object events() {
    return service.events();
  }

  @PostMapping("/events")
  public Object event(@Valid @RequestBody Event e) {
    return service.event(e);
  }

  @GetMapping("/events/{id}/participants")
  public Object participants(@PathVariable long id) {
    return service.participants(id);
  }

  @PostMapping("/events/{id}/participants")
  public Object participant(@PathVariable long id, @Valid @RequestBody Participant p) {
    return service.participant(id, p);
  }
}
