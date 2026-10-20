package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for staff_leave. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "staff_leave")
public class StaffLeaveEntity {
  public StaffLeaveEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "starts_at", nullable = false)
  private OffsetDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private OffsetDateTime endsAt;

  @Column(name = "reason", nullable = true, length = 300)
  private String reason;

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

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    this.reason = value;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }
}
