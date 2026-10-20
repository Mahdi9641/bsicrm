package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for invoice_links. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "invoice_links")
public class InvoiceLinksEntity {
  public InvoiceLinksEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "invoice_id", nullable = false)
  private Long invoiceId;

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

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

  public Long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(Long value) {
    this.invoiceId = value;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String value) {
    this.tokenHash = value;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime value) {
    this.expiresAt = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public InvoicesEntity getInvoiceReference() {
    return invoiceReference;
  }
}
