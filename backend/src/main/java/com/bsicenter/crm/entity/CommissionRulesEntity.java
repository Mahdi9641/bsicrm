package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for commission_rules. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "commission_rules")
public class CommissionRulesEntity {
  public CommissionRulesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "kind", nullable = false, length = 16)
  private String kind;

  @Column(name = "rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal rate;

  @Column(name = "fixed_amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal fixedAmount;

  @Column(name = "threshold", nullable = false, precision = 16, scale = 0)
  private BigDecimal threshold;

  @Column(name = "upper_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal upperRate;

  @Column(name = "valid_from", nullable = false)
  private LocalDate validFrom;

  @Column(name = "valid_to", nullable = false)
  private LocalDate validTo;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ServicesEntity serviceReference;

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

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal value) {
    this.rate = value;
  }

  public BigDecimal getFixedAmount() {
    return fixedAmount;
  }

  public void setFixedAmount(BigDecimal value) {
    this.fixedAmount = value;
  }

  public BigDecimal getThreshold() {
    return threshold;
  }

  public void setThreshold(BigDecimal value) {
    this.threshold = value;
  }

  public BigDecimal getUpperRate() {
    return upperRate;
  }

  public void setUpperRate(BigDecimal value) {
    this.upperRate = value;
  }

  public LocalDate getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(LocalDate value) {
    this.validFrom = value;
  }

  public LocalDate getValidTo() {
    return validTo;
  }

  public void setValidTo(LocalDate value) {
    this.validTo = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
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

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }

  public ServicesEntity getServiceReference() {
    return serviceReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
