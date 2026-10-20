package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for account_transfers. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "account_transfers")
public class AccountTransfersEntity {
  public AccountTransfersEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "from_account_id", nullable = false)
  private Long fromAccountId;

  @Column(name = "to_account_id", nullable = false)
  private Long toAccountId;

  @Column(name = "amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal amount;

  @Column(name = "reason", nullable = false, length = 300)
  private String reason;

  @Column(name = "idempotency_key", nullable = false)
  private UUID idempotencyKey;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_account_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AccountsEntity fromAccountReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_account_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AccountsEntity toAccountReference;

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

  public Long getFromAccountId() {
    return fromAccountId;
  }

  public void setFromAccountId(Long value) {
    this.fromAccountId = value;
  }

  public Long getToAccountId() {
    return toAccountId;
  }

  public void setToAccountId(Long value) {
    this.toAccountId = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    this.reason = value;
  }

  public UUID getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(UUID value) {
    this.idempotencyKey = value;
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

  public AccountsEntity getFromAccountReference() {
    return fromAccountReference;
  }

  public AccountsEntity getToAccountReference() {
    return toAccountReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
