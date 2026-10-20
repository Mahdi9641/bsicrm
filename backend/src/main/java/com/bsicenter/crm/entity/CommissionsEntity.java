package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for commissions. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "commissions")
public class CommissionsEntity {
  public CommissionsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "invoice_item_id", nullable = false)
  private Long invoiceItemId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "basis", nullable = false, precision = 16, scale = 0)
  private BigDecimal basis;

  @Column(name = "rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal rate;

  @Column(name = "amount", nullable = false, precision = 16, scale = 0)
  private BigDecimal amount;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "settled_at", nullable = true)
  private OffsetDateTime settledAt;

  @Column(name = "rule_id", nullable = true)
  private Long ruleId;

  @Column(name = "rule_snapshot", nullable = true, length = 500)
  private String ruleSnapshot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_item_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private InvoiceItemsEntity invoiceItemReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rule_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CommissionRulesEntity ruleReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getInvoiceItemId() {
    return invoiceItemId;
  }

  public void setInvoiceItemId(Long value) {
    this.invoiceItemId = value;
  }

  public Long getStaffId() {
    return staffId;
  }

  public void setStaffId(Long value) {
    this.staffId = value;
  }

  public BigDecimal getBasis() {
    return basis;
  }

  public void setBasis(BigDecimal value) {
    this.basis = value;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal value) {
    this.rate = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public OffsetDateTime getSettledAt() {
    return settledAt;
  }

  public void setSettledAt(OffsetDateTime value) {
    this.settledAt = value;
  }

  public Long getRuleId() {
    return ruleId;
  }

  public void setRuleId(Long value) {
    this.ruleId = value;
  }

  public String getRuleSnapshot() {
    return ruleSnapshot;
  }

  public void setRuleSnapshot(String value) {
    this.ruleSnapshot = value;
  }

  public InvoiceItemsEntity getInvoiceItemReference() {
    return invoiceItemReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }

  public CommissionRulesEntity getRuleReference() {
    return ruleReference;
  }
}
