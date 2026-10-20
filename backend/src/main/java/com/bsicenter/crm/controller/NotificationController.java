package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.NotificationRequests.*;
import com.bsicenter.crm.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NotificationController {
  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping("/message-templates")
  public Object templates() {
    return service.templates();
  }

  @PutMapping("/message-templates/{id}")
  public Object template(@PathVariable long id, @Valid @RequestBody Template body) {
    return service.update(id, body);
  }

  @PostMapping("/invoices/{id}/sms")
  public Object invoice(@PathVariable long id, @Valid @RequestBody InvoiceSms body) {
    return service.invoice(id, body);
  }

  @PostMapping("/messages/{id}/cancel")
  public Object cancel(@PathVariable long id) {
    return service.cancel(id);
  }

  @PostMapping("/integrations/sms/delivery")
  public Object delivery(
      @RequestHeader(value = "X-SMS-Secret", required = false) String secret,
      @Valid @RequestBody Delivery body) {
    return service.delivery(secret, body);
  }
}
