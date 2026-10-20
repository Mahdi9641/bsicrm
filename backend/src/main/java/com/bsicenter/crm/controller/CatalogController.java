package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.CatalogRequests.*;
import com.bsicenter.crm.service.CatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
  private final CatalogService service;

  public CatalogController(CatalogService service) {
    this.service = service;
  }

  @GetMapping("/bootstrap")
  public Object bootstrap() {
    return service.bootstrap();
  }

  @GetMapping("/customers")
  public Object customers(
      @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page) {
    return service.customers(q, page);
  }

  @GetMapping("/customers/{id}")
  public Object customer(@PathVariable long id) {
    return service.customer(id);
  }

  @PostMapping("/customers")
  public Object createCustomer(@Valid @RequestBody Customer c) {
    return service.createCustomer(c);
  }

  @PutMapping("/customers/{id}")
  public Object editCustomer(@PathVariable long id, @Valid @RequestBody Customer c) {
    return service.editCustomer(id, c);
  }

  @PostMapping("/customers/{id}/preferences")
  public Object preference(@PathVariable long id, @Valid @RequestBody Preference p) {
    return service.preference(id, p);
  }

  @PostMapping("/customers/{id}/occasions")
  public Object occasion(@PathVariable long id, @Valid @RequestBody Occasion p) {
    return service.occasion(id, p);
  }

  @PostMapping("/customers/{id}/relations")
  public Object relation(@PathVariable long id, @Valid @RequestBody Relation body) {
    return service.relation(id, body);
  }

  @DeleteMapping("/customers/{id}/relations/{relationId}")
  public Object removeRelation(@PathVariable long id, @PathVariable long relationId) {
    return service.removeRelation(id, relationId);
  }

  @GetMapping("/services")
  public Object services() {
    return service.services();
  }

  @PostMapping("/services")
  public Object service(@Valid @RequestBody Service s) {
    return service.service(s);
  }

  @PutMapping("/services/{id}")
  public Object serviceEdit(@PathVariable long id, @Valid @RequestBody Service s) {
    return service.serviceEdit(id, s);
  }

  @GetMapping("/staff")
  public Object staff() {
    return service.staff();
  }

  @PostMapping("/staff")
  public Object staff(@Valid @RequestBody Staff s) {
    return service.staff(s);
  }

  @PutMapping("/staff/{id}")
  public Object staffEdit(@PathVariable long id, @Valid @RequestBody Staff s) {
    return service.staffEdit(id, s);
  }

  @GetMapping("/staff/{id}/leave")
  public Object leaves(@PathVariable long id) {
    return service.leaves(id);
  }

  @PostMapping("/staff/{id}/leave")
  public Object leave(@PathVariable long id, @Valid @RequestBody Leave l) {
    return service.leave(id, l);
  }

  @GetMapping("/products")
  public Object products() {
    return service.products();
  }

  @PostMapping("/products")
  public Object product(@Valid @RequestBody Product p) {
    return service.product(p);
  }

  @PutMapping("/products/{id}")
  public Object productEdit(@PathVariable long id, @Valid @RequestBody Product p) {
    return service.productEdit(id, p);
  }

  @PostMapping("/products/{id}/stock")
  public Object stock(@PathVariable long id, @Valid @RequestBody Stock p) {
    return service.stock(id, p);
  }

  @GetMapping("/users")
  public Object users() {
    return service.users();
  }

  @PostMapping("/users")
  public Object user(@Valid @RequestBody User u) {
    return service.user(u);
  }

  @PostMapping("/users/{id}/password")
  public Object userPassword(@PathVariable long id, @Valid @RequestBody UserPassword body) { return service.userPassword(id, body); }

  @PostMapping("/users/{id}/deactivate")
  public Object deactivate(@PathVariable long id) {
    return service.deactivate(id);
  }

  @GetMapping("/branches")
  public Object branches() {
    return service.branches();
  }

  @PostMapping("/branches")
  public Object branch(@Valid @RequestBody Name n) {
    return service.branch(n);
  }

  @PutMapping("/settings")
  public Object settings(@Valid @RequestBody Settings s) {
    return service.settings(s);
  }

  @GetMapping("/audit")
  public Object audit() {
    return service.audit();
  }
}
