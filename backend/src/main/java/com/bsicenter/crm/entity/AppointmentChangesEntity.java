package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for appointment_changes. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "appointment_changes")
public class AppointmentChangesEntity {
  public AppointmentChangesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "appointment_id", nullable = false)
  private Long appointmentId;

  @Column(name = "changed_by", nullable = false)
  private Long changedBy;

  @Column(name = "old_start", nullable = false)
  private OffsetDateTime oldStart;

  @Column(name = "new_start", nullable = false)
  private OffsetDateTime newStart;

  @Column(name = "reason", nullable = false, length = 500)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppointmentsEntity appointmentReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity changedByReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long value) {
    this.appointmentId = value;
  }

  public Long getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(Long value) {
    this.changedBy = value;
  }

  public OffsetDateTime getOldStart() {
    return oldStart;
  }

  public void setOldStart(OffsetDateTime value) {
    this.oldStart = value;
  }

  public OffsetDateTime getNewStart() {
    return newStart;
  }

  public void setNewStart(OffsetDateTime value) {
    this.newStart = value;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    this.reason = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public AppointmentsEntity getAppointmentReference() {
    return appointmentReference;
  }

  public AppUsersEntity getChangedByReference() {
    return changedByReference;
  }
}
