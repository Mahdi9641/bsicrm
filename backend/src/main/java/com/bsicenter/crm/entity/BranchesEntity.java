package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for branches. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "branches")
public class BranchesEntity {
  public BranchesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "active", nullable = false)
  private Boolean active;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
  }
}
