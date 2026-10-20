package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.ResourceRequests.*;
import com.bsicenter.crm.service.ResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
  private final ResourceService service;

  public ResourceController(ResourceService service) {
    this.service = service;
  }

  @GetMapping
  public List<View> list() {
    return service.list();
  }

  @PostMapping
  public View create(@Valid @RequestBody Write body) {
    return service.save(null, body);
  }

  @PutMapping("/{id}")
  public View update(@PathVariable long id, @Valid @RequestBody Write body) {
    return service.save(id, body);
  }
}
