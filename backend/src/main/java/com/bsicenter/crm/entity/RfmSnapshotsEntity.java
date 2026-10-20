package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for rfm_snapshots. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "rfm_snapshots")
public class RfmSnapshotsEntity {
  public RfmSnapshotsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "calculated_on", nullable = false)
  private LocalDate calculatedOn;

  @Column(name = "recency", nullable = true)
  private Integer recency;

  @Column(name = "frequency", nullable = false)
  private Integer frequency;

  @Column(name = "monetary", nullable = false, precision = 16, scale = 0)
  private BigDecimal monetary;

  @Column(name = "segment", nullable = false, length = 20)
  private String segment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

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

  public LocalDate getCalculatedOn() {
    return calculatedOn;
  }

  public void setCalculatedOn(LocalDate value) {
    this.calculatedOn = value;
  }

  public Integer getRecency() {
    return recency;
  }

  public void setRecency(Integer value) {
    this.recency = value;
  }

  public Integer getFrequency() {
    return frequency;
  }

  public void setFrequency(Integer value) {
    this.frequency = value;
  }

  public BigDecimal getMonetary() {
    return monetary;
  }

  public void setMonetary(BigDecimal value) {
    this.monetary = value;
  }

  public String getSegment() {
    return segment;
  }

  public void setSegment(String value) {
    this.segment = value;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }
}
