package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for staff_services. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "staff_services")
@IdClass(StaffServicesEntity.Key.class)
public class StaffServicesEntity {
  public StaffServicesEntity() {}

  @Id
  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Id
  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ServicesEntity serviceReference;

  public Long getStaffId() {
    return staffId;
  }

  public void setStaffId(Long value) {
    this.staffId = value;
  }

  public Long getServiceId() {
    return serviceId;
  }

  public void setServiceId(Long value) {
    this.serviceId = value;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }

  public ServicesEntity getServiceReference() {
    return serviceReference;
  }

  public static class Key implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public Long serviceId;
    public Long staffId;

    public Key() {}

    @Override
    public boolean equals(Object other) {
      return other instanceof Key k
          && Objects.equals(serviceId, k.serviceId)
          && Objects.equals(staffId, k.staffId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(serviceId, staffId);
    }
  }
}
