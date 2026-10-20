package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for products. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "products")
public class ProductsEntity {
  public ProductsEntity() {}

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

  @Column(name = "price", nullable = false, precision = 16, scale = 0)
  private BigDecimal price;

  @Column(name = "cost", nullable = false, precision = 16, scale = 0)
  private BigDecimal cost;

  @Column(name = "stock", nullable = false)
  private Integer stock;

  @Column(name = "minimum_stock", nullable = false)
  private Integer minimumStock;

  @Column(name = "supplier", nullable = true, length = 160)
  private String supplier;

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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal value) {
    this.price = value;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal value) {
    this.cost = value;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer value) {
    this.stock = value;
  }

  public Integer getMinimumStock() {
    return minimumStock;
  }

  public void setMinimumStock(Integer value) {
    this.minimumStock = value;
  }

  public String getSupplier() {
    return supplier;
  }

  public void setSupplier(String value) {
    this.supplier = value;
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
