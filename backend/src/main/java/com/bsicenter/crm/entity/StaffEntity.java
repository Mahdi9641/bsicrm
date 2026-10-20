package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for staff. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "staff")
public class StaffEntity {
  public StaffEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "mobile", nullable = false, length = 20)
  private String mobile;

  @Column(name = "specialty", nullable = false, length = 160)
  private String specialty;

  @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal commissionRate;

  @Column(name = "work_start", nullable = false)
  private LocalTime workStart;

  @Column(name = "work_end", nullable = false)
  private LocalTime workEnd;

  @Column(name = "work_days", nullable = false, length = 30)
  private String workDays;

  @Column(name = "active", nullable = false)
  private Boolean active;

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

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String value) {
    this.mobile = value;
  }

  public String getSpecialty() {
    return specialty;
  }

  public void setSpecialty(String value) {
    this.specialty = value;
  }

  public BigDecimal getCommissionRate() {
    return commissionRate;
  }

  public void setCommissionRate(BigDecimal value) {
    this.commissionRate = value;
  }

  public LocalTime getWorkStart() {
    return workStart;
  }

  public void setWorkStart(LocalTime value) {
    this.workStart = value;
  }

  public LocalTime getWorkEnd() {
    return workEnd;
  }

  public void setWorkEnd(LocalTime value) {
    this.workEnd = value;
  }

  public String getWorkDays() {
    return workDays;
  }

  public void setWorkDays(String value) {
    this.workDays = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }
}
