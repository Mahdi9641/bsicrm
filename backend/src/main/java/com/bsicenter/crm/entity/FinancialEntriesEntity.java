package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for financial_entries. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "financial_entries")
public class FinancialEntriesEntity {
  public FinancialEntriesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Column(name = "amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal amount;

  @Column(name = "kind", nullable = false, length = 30)
  private String kind;

  @Column(name = "payment_id", nullable = true)
  private Long paymentId;

  @Column(name = "expense_id", nullable = true)
  private Long expenseId;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "transfer_id", nullable = true)
  private Long transferId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AccountsEntity accountReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private PaymentsEntity paymentReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "expense_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ExpensesEntity expenseReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transfer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AccountTransfersEntity transferReference;

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

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
  }

  public Long getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(Long value) {
    this.paymentId = value;
  }

  public Long getExpenseId() {
    return expenseId;
  }

  public void setExpenseId(Long value) {
    this.expenseId = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public Long getTransferId() {
    return transferId;
  }

  public void setTransferId(Long value) {
    this.transferId = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public AccountsEntity getAccountReference() {
    return accountReference;
  }

  public PaymentsEntity getPaymentReference() {
    return paymentReference;
  }

  public ExpensesEntity getExpenseReference() {
    return expenseReference;
  }

  public AccountTransfersEntity getTransferReference() {
    return transferReference;
  }
}
