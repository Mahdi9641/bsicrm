package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for visits. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "visits")
public class VisitsEntity {
  public VisitsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "appointment_id", nullable = true)
  private Long appointmentId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "priority", nullable = false, length = 10)
  private String priority;

  @Column(name = "check_in_at", nullable = false)
  private OffsetDateTime checkInAt;

  @Column(name = "completed_at", nullable = true)
  private OffsetDateTime completedAt;

  @Column(name = "notes", nullable = true, length = 1000)
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppointmentsEntity appointmentReference;

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

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long value) {
    this.appointmentId = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String value) {
    this.priority = value;
  }

  public OffsetDateTime getCheckInAt() {
    return checkInAt;
  }

  public void setCheckInAt(OffsetDateTime value) {
    this.checkInAt = value;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(OffsetDateTime value) {
    this.completedAt = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public AppointmentsEntity getAppointmentReference() {
    return appointmentReference;
  }
}
