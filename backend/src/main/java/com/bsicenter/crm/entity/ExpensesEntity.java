package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for expenses. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "expenses")
public class ExpensesEntity {
  public ExpensesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Column(name = "title", nullable = false, length = 160)
  private String title;

  @Column(name = "category", nullable = false, length = 80)
  private String category;

  @Column(name = "amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal amount;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AccountsEntity accountReference;

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

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long value) {
    this.accountId = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    this.category = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
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

  public AccountsEntity getAccountReference() {
    return accountReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
