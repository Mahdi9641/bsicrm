package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for audit_logs. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "audit_logs")
public class AuditLogsEntity {
  public AuditLogsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "user_id", nullable = true)
  private Long userId;

  @Column(name = "action", nullable = false, length = 40)
  private String action;

  @Column(name = "entity", nullable = false, length = 50)
  private String entity;

  @Column(name = "entity_id", nullable = true)
  private Long entityId;

  @Column(name = "detail", nullable = true, length = 500)
  private String detail;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity userReference;

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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long value) {
    this.userId = value;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String value) {
    this.action = value;
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(String value) {
    this.entity = value;
  }

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long value) {
    this.entityId = value;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String value) {
    this.detail = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public AppUsersEntity getUserReference() {
    return userReference;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }
}
