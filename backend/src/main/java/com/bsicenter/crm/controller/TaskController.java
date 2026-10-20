package com.bsicenter.crm.controller;

import com.bsicenter.crm.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  private final TaskService service;

  public TaskController(TaskService service) {
    this.service = service;
  }

  @GetMapping
  Object list(
      @RequestParam(defaultValue = "ACTIVE") String status,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "false") boolean mine,
      @RequestParam(defaultValue = "false") boolean overdue,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size) {
    return service.list(status, q, mine, overdue, page, size);
  }

  @GetMapping("/summary")
  public Object summary() {
    return service.summary();
  }

  @GetMapping("/assignees")
  public Object assignees() {
    return service.assignees();
  }

  @GetMapping("/{id}")
  public Object detail(@PathVariable long id) {
    return service.detail(id);
  }

  @PostMapping
  public Object create(@Valid @RequestBody com.bsicenter.crm.dto.TaskRequests.Write body) {
    return service.create(body);
  }

  @PutMapping("/{id}")
  public Object update(
      @PathVariable long id, @Valid @RequestBody com.bsicenter.crm.dto.TaskRequests.Write body) {
    return service.update(id, body);
  }

  @PostMapping("/{id}/status")
  public Object status(
      @PathVariable long id,
      @Valid @RequestBody com.bsicenter.crm.dto.TaskRequests.Transition body) {
    return service.transition(id, body);
  }

  @PostMapping("/{id}/comments")
  public Object comment(
      @PathVariable long id, @Valid @RequestBody com.bsicenter.crm.dto.TaskRequests.Comment body) {
    return service.comment(id, body);
  }
}
