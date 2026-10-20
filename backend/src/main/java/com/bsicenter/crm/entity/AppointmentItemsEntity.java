package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for appointment_items. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "appointment_items")
public class AppointmentItemsEntity {
  public AppointmentItemsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "appointment_id", nullable = false)
  private Long appointmentId;

  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "starts_at", nullable = false)
  private OffsetDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private OffsetDateTime endsAt;

  @Column(name = "blocked_until", nullable = false)
  private OffsetDateTime blockedUntil;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "resource_id", nullable = true)
  private Long resourceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppointmentsEntity appointmentReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ServicesEntity serviceReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resource_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private SalonResourcesEntity resourceReference;

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

  public Long getServiceId() {
    return serviceId;
  }

  public void setServiceId(Long value) {
    this.serviceId = value;
  }

  public Long getStaffId() {
    return staffId;
  }

  public void setStaffId(Long value) {
    this.staffId = value;
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

  public OffsetDateTime getBlockedUntil() {
    return blockedUntil;
  }

  public void setBlockedUntil(OffsetDateTime value) {
    this.blockedUntil = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(Long value) {
    this.resourceId = value;
  }

  public AppointmentsEntity getAppointmentReference() {
    return appointmentReference;
  }

  public ServicesEntity getServiceReference() {
    return serviceReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }

  public SalonResourcesEntity getResourceReference() {
    return resourceReference;
  }
}
