package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.OperationsRequests.*;
import com.bsicenter.crm.service.OperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OperationsController {
  private final OperationsService service;

  public OperationsController(OperationsService service) {
    this.service = service;
  }

  @GetMapping("/appointments")
  public Object appointments(@RequestParam OffsetDateTime from, @RequestParam OffsetDateTime to) {
    return service.appointments(from, to);
  }

  @PostMapping("/appointments")
  public Object book(@Valid @RequestBody com.bsicenter.crm.dto.BookingRequests.Create body) {
    return service.book(body);
  }

  @GetMapping("/appointments/{id}")
  public Object appointment(@PathVariable long id) {
    return service.appointment(id);
  }

  @PostMapping("/appointments/availability")
  public Object availability(
      @Valid @RequestBody com.bsicenter.crm.dto.BookingRequests.Availability body) {
    return service.availability(body);
  }

  @PutMapping("/appointments/{id}/schedule")
  public Object reschedule(
      @PathVariable long id,
      @Valid @RequestBody com.bsicenter.crm.dto.BookingRequests.Reschedule body) {
    return service.reschedule(id, body);
  }

  @PostMapping("/appointments/{id}/status")
  public Object status(@PathVariable long id, @Valid @RequestBody Status s) {
    return service.status(id, s);
  }

  @PostMapping("/visits")
  public Object checkIn(@Valid @RequestBody CheckIn c) {
    return service.checkIn(c);
  }

  @GetMapping("/visits")
  public Object visits() {
    return service.visits();
  }

  @PostMapping("/visits/{id}/services")
  public Object addService(@PathVariable long id, @Valid @RequestBody BookingItem b) {
    return service.addService(id, b);
  }

  @PostMapping("/visit-services/{id}/status")
  public Object serviceStatus(@PathVariable long id, @Valid @RequestBody ServiceStatus s) {
    return service.serviceStatus(id, s);
  }

  @PostMapping("/visits/{id}/complete")
  public Object complete(@PathVariable long id) {
    return service.complete(id);
  }
}
