package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for follow_up_tasks. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "follow_up_tasks")
public class FollowUpTasksEntity {
  public FollowUpTasksEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "customer_id", nullable = true)
  private Long customerId;

  @Column(name = "assigned_to", nullable = false)
  private Long assignedTo;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "title", nullable = false, length = 160)
  private String title;

  @Column(name = "description", nullable = false, length = 2000)
  private String description;

  @Column(name = "due_at", nullable = false)
  private OffsetDateTime dueAt;

  @Column(name = "priority", nullable = false, length = 10)
  private String priority;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "completed_at", nullable = true)
  private OffsetDateTime completedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity assignedToReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity createdByReference;

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

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public Long getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(Long value) {
    this.assignedTo = value;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long value) {
    this.createdBy = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public OffsetDateTime getDueAt() {
    return dueAt;
  }

  public void setDueAt(OffsetDateTime value) {
    this.dueAt = value;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String value) {
    this.priority = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    this.version = value;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(OffsetDateTime value) {
    this.completedAt = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime value) {
    this.updatedAt = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public AppUsersEntity getAssignedToReference() {
    return assignedToReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
