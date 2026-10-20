package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for visit_services. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "visit_services")
public class VisitServicesEntity {
  public VisitServicesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "visit_id", nullable = false)
  private Long visitId;

  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "started_at", nullable = true)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at", nullable = true)
  private OffsetDateTime completedAt;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "rating", nullable = true)
  private Integer rating;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visit_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private VisitsEntity visitReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ServicesEntity serviceReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getVisitId() {
    return visitId;
  }

  public void setVisitId(Long value) {
    this.visitId = value;
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

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(OffsetDateTime value) {
    this.startedAt = value;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(OffsetDateTime value) {
    this.completedAt = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer value) {
    this.rating = value;
  }

  public VisitsEntity getVisitReference() {
    return visitReference;
  }

  public ServicesEntity getServiceReference() {
    return serviceReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }
}
