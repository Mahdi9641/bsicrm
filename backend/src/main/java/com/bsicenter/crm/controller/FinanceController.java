package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.FinanceRequests.*;
import com.bsicenter.crm.service.FinanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {
  private final FinanceService service;

  public FinanceController(FinanceService service) {
    this.service = service;
  }

  @PostMapping("/invoices")
  public Object invoice(@Valid @RequestBody Invoice b) {
    return service.invoice(b);
  }

  @GetMapping("/invoices")
  public Object invoices() {
    return service.invoices();
  }

  @GetMapping("/invoices/{id}")
  public Object detail(@PathVariable long id) {
    return service.detail(id);
  }

  @PostMapping("/invoices/{id}/payments")
  public Object pay(@PathVariable long id, @Valid @RequestBody Payment p) {
    return service.pay(id, p);
  }

  @PostMapping("/invoices/{id}/void")
  public Object voidInvoice(@PathVariable long id, @Valid @RequestBody Reason r) {
    return service.voidInvoice(id, r);
  }

  @GetMapping("/finance")
  public Object finance() {
    return service.finance();
  }

  @PostMapping("/expenses")
  public Object expense(@Valid @RequestBody Expense e) {
    return service.expense(e);
  }

  @GetMapping("/commissions")
  public Object commissions() {
    return service.commissions();
  }

  @PostMapping("/commissions/{id}/status")
  public Object settle(@PathVariable long id, @Valid @RequestBody Settlement s) {
    return service.settle(id, s);
  }

  @PostMapping("/invoices/{id}/link")
  public Object invoiceLink(@PathVariable long id) {
    return service.invoiceLink(id);
  }

  @GetMapping("/public/invoices/{token}")
  public Object publicInvoice(@PathVariable String token) {
    return service.publicInvoice(token);
  }
}
