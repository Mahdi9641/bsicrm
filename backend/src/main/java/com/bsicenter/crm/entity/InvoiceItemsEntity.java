package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for invoice_items. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "invoice_items")
public class InvoiceItemsEntity {
  public InvoiceItemsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "invoice_id", nullable = false)
  private Long invoiceId;

  @Column(name = "service_id", nullable = true)
  private Long serviceId;

  @Column(name = "product_id", nullable = true)
  private Long productId;

  @Column(name = "staff_id", nullable = true)
  private Long staffId;

  @Column(name = "description", nullable = false, length = 200)
  private String description;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 16, scale = 0)
  private BigDecimal unitPrice;

  @Column(name = "unit_cost", nullable = false, precision = 16, scale = 0)
  private BigDecimal unitCost;

  @Column(name = "discount", nullable = false, precision = 16, scale = 0)
  private BigDecimal discount;

  @Column(name = "total", nullable = false, precision = 16, scale = 0)
  private BigDecimal total;

  @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal commissionRate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private InvoicesEntity invoiceReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ServicesEntity serviceReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ProductsEntity productReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity staffReference;

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

  public Long getServiceId() {
    return serviceId;
  }

  public void setServiceId(Long value) {
    this.serviceId = value;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long value) {
    this.productId = value;
  }

  public Long getStaffId() {
    return staffId;
  }

  public void setStaffId(Long value) {
    this.staffId = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    this.quantity = value;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal value) {
    this.unitPrice = value;
  }

  public BigDecimal getUnitCost() {
    return unitCost;
  }

  public void setUnitCost(BigDecimal value) {
    this.unitCost = value;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public void setDiscount(BigDecimal value) {
    this.discount = value;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal value) {
    this.total = value;
  }

  public BigDecimal getCommissionRate() {
    return commissionRate;
  }

  public void setCommissionRate(BigDecimal value) {
    this.commissionRate = value;
  }

  public InvoicesEntity getInvoiceReference() {
    return invoiceReference;
  }

  public ServicesEntity getServiceReference() {
    return serviceReference;
  }

  public ProductsEntity getProductReference() {
    return productReference;
  }

  public StaffEntity getStaffReference() {
    return staffReference;
  }
}
