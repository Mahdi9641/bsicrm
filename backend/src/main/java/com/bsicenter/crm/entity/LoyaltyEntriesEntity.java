package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for loyalty_entries. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "loyalty_entries")
public class LoyaltyEntriesEntity {
  public LoyaltyEntriesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "invoice_id", nullable = true)
  private Long invoiceId;

  @Column(name = "points", nullable = false)
  private Integer points;

  @Column(name = "reason", nullable = false, length = 300)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private InvoicesEntity invoiceReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(Long value) {
    this.invoiceId = value;
  }

  public Integer getPoints() {
    return points;
  }

  public void setPoints(Integer value) {
    this.points = value;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    this.reason = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public InvoicesEntity getInvoiceReference() {
    return invoiceReference;
  }
}
