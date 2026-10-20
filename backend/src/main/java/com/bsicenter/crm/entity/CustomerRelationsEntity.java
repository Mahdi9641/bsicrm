package com.bsicenter.crm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_relations")
public class CustomerRelationsEntity {
  public CustomerRelationsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    id = value;
  }

  @Column(name = "customer_id")
  private Long customerId;

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    customerId = value;
  }

  @Column(name = "relationship")
  private String relationship;

  public String getRelationship() {
    return relationship;
  }

  public void setRelationship(String value) {
    relationship = value;
  }

  @Column(name = "full_name")
  private String fullName;

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String value) {
    fullName = value;
  }

  @Column(name = "birth_date")
  private java.time.LocalDate birthDate;

  public java.time.LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(java.time.LocalDate value) {
    birthDate = value;
  }

  @Column(name = "notes")
  private String notes;

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    notes = value;
  }

  @Column(name = "created_at")
  private java.time.OffsetDateTime createdAt;

  public java.time.OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(java.time.OffsetDateTime value) {
    createdAt = value;
  }
}
