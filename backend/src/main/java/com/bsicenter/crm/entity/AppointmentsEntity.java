package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for appointments. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "appointments")
public class AppointmentsEntity {
  public AppointmentsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "starts_at", nullable = false)
  private OffsetDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private OffsetDateTime endsAt;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "source", nullable = false, length = 30)
  private String source;

  @Column(name = "notes", nullable = true, length = 1000)
  private String notes;

  @Column(name = "cancel_reason", nullable = true, length = 500)
  private String cancelReason;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

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

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String value) {
    this.source = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public void setCancelReason(String value) {
    this.cancelReason = value;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long value) {
    this.createdBy = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    this.version = value;
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

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
