package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for salon_resources. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "salon_resources")
public class SalonResourcesEntity {
  public SalonResourcesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "kind", nullable = false, length = 20)
  private String kind;

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

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
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
