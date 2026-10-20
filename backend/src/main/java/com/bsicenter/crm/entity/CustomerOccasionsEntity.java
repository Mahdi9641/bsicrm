package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for customer_occasions. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "customer_occasions")
public class CustomerOccasionsEntity {
  public CustomerOccasionsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "title", nullable = false, length = 120)
  private String title;

  @Column(name = "occasion_date", nullable = false)
  private LocalDate occasionDate;

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

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public LocalDate getOccasionDate() {
    return occasionDate;
  }

  public void setOccasionDate(LocalDate value) {
    this.occasionDate = value;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }
}
