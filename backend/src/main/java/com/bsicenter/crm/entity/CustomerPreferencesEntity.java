package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for customer_preferences. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "customer_preferences")
public class CustomerPreferencesEntity {
  public CustomerPreferencesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "category", nullable = false, length = 60)
  private String category;

  @Column(name = "value", nullable = false, length = 500)
  private String value;

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

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    this.category = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }
}
