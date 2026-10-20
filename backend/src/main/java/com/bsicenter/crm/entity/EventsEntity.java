package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for events. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "events")
public class EventsEntity {
  public EventsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "description", nullable = true, length = 1000)
  private String description;

  @Column(name = "starts_at", nullable = false)
  private OffsetDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private OffsetDateTime endsAt;

  @Column(name = "venue", nullable = false, length = 250)
  private String venue;

  @Column(name = "capacity", nullable = false)
  private Integer capacity;

  @Column(name = "budget", nullable = false, precision = 16, scale = 0)
  private BigDecimal budget;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getBranchId() {
    return branchId;
  }

  public void setBranchId(Long value) {
    this.branchId = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public OffsetDateTime getStartsAt() {
    return startsAt;
  }

  public void setStartsAt(OffsetDateTime value) {
    this.startsAt = value;
  }

  public OffsetDateTime getEndsAt() {
    return endsAt;
  }

  public void setEndsAt(OffsetDateTime value) {
    this.endsAt = value;
  }

  public String getVenue() {
    return venue;
  }

  public void setVenue(String value) {
    this.venue = value;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer value) {
    this.capacity = value;
  }

  public BigDecimal getBudget() {
    return budget;
  }

  public void setBudget(BigDecimal value) {
    this.budget = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }
}
