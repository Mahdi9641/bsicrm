package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for services. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "services")
public class ServicesEntity {
  public ServicesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "category", nullable = false, length = 80)
  private String category;

  @Column(name = "description", nullable = true, length = 500)
  private String description;

  @Column(name = "price", nullable = false, precision = 16, scale = 0)
  private BigDecimal price;

  @Column(name = "material_cost", nullable = false, precision = 16, scale = 0)
  private BigDecimal materialCost;

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column(name = "buffer_minutes", nullable = false)
  private Integer bufferMinutes;

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

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    this.category = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal value) {
    this.price = value;
  }

  public BigDecimal getMaterialCost() {
    return materialCost;
  }

  public void setMaterialCost(BigDecimal value) {
    this.materialCost = value;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer value) {
    this.durationMinutes = value;
  }

  public Integer getBufferMinutes() {
    return bufferMinutes;
  }

  public void setBufferMinutes(Integer value) {
    this.bufferMinutes = value;
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
