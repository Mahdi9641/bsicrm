package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for invoices. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "invoices")
public class InvoicesEntity {
  public InvoicesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "visit_id", nullable = true)
  private Long visitId;

  @Column(name = "total", nullable = false, precision = 16, scale = 0)
  private BigDecimal total;

  @Column(name = "discount", nullable = false, precision = 16, scale = 0)
  private BigDecimal discount;

  @Column(name = "tax", nullable = false, precision = 16, scale = 0)
  private BigDecimal tax;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "void_reason", nullable = true, length = 500)
  private String voidReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visit_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private VisitsEntity visitReference;

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

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public Long getVisitId() {
    return visitId;
  }

  public void setVisitId(Long value) {
    this.visitId = value;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal value) {
    this.total = value;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public void setDiscount(BigDecimal value) {
    this.discount = value;
  }

  public BigDecimal getTax() {
    return tax;
  }

  public void setTax(BigDecimal value) {
    this.tax = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
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

  public String getVoidReason() {
    return voidReason;
  }

  public void setVoidReason(String value) {
    this.voidReason = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public VisitsEntity getVisitReference() {
    return visitReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
