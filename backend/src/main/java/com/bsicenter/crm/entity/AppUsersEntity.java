package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for app_users. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "app_users")
public class AppUsersEntity {
  public AppUsersEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "username", nullable = false, length = 80)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "role", nullable = false, length = 20)
  private String role;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "staff_id", nullable = true)
  private Long staffId;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "failed_attempts", nullable = false)
  private Integer failedAttempts;

  @Column(name = "locked_until", nullable = true)
  private OffsetDateTime lockedUntil;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    this.username = value;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String value) {
    this.passwordHash = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String value) {
    this.role = value;
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

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
  }

  public Integer getFailedAttempts() {
    return failedAttempts;
  }

  public void setFailedAttempts(Integer value) {
    this.failedAttempts = value;
  }

  public OffsetDateTime getLockedUntil() {
    return lockedUntil;
  }

  public void setLockedUntil(OffsetDateTime value) {
    this.lockedUntil = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }
}
