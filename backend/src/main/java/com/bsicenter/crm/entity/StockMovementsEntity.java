package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for stock_movements. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "stock_movements")
public class StockMovementsEntity {
  public StockMovementsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "reason", nullable = false, length = 300)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ProductsEntity productReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long value) {
    this.productId = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    this.quantity = value;
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

  public ProductsEntity getProductReference() {
    return productReference;
  }
}
