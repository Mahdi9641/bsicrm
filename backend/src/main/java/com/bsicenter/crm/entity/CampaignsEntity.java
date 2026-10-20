package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for campaigns. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "campaigns")
public class CampaignsEntity {
  public CampaignsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "objective", nullable = true, length = 300)
  private String objective;

  @Column(name = "segment", nullable = false, length = 20)
  private String segment;

  @Column(name = "body", nullable = false, length = 1000)
  private String body;

  @Column(name = "budget", nullable = false, precision = 16, scale = 0)
  private BigDecimal budget;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

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

  public String getObjective() {
    return objective;
  }

  public void setObjective(String value) {
    this.objective = value;
  }

  public String getSegment() {
    return segment;
  }

  public void setSegment(String value) {
    this.segment = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    this.body = value;
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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }
}
