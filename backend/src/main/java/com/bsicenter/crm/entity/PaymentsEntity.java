package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for payments. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "payments")
public class PaymentsEntity {
  public PaymentsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "invoice_id", nullable = false)
  private Long invoiceId;

  @Column(name = "amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal amount;

  @Column(name = "method", nullable = false, length = 20)
  private String method;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Column(name = "tracking_code", nullable = true, length = 100)
  private String trackingCode;

  @Column(name = "idempotency_key", nullable = false)
  private UUID idempotencyKey;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "reversed", nullable = false)
  private Boolean reversed;

  @Column(name = "reversal_reason", nullable = true, length = 500)
  private String reversalReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private InvoicesEntity invoiceReference;

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

  public Long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(Long value) {
    this.invoiceId = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String value) {
    this.method = value;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long value) {
    this.accountId = value;
  }

  public String getTrackingCode() {
    return trackingCode;
  }

  public void setTrackingCode(String value) {
    this.trackingCode = value;
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

  public Boolean getReversed() {
    return reversed;
  }

  public void setReversed(Boolean value) {
    this.reversed = value;
  }

  public String getReversalReason() {
    return reversalReason;
  }

  public void setReversalReason(String value) {
    this.reversalReason = value;
  }

  public InvoicesEntity getInvoiceReference() {
    return invoiceReference;
  }

  public AccountsEntity getAccountReference() {
    return accountReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
