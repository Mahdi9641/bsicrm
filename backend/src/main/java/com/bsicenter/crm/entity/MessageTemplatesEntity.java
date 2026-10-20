package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for message_templates. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "message_templates")
public class MessageTemplatesEntity {
  public MessageTemplatesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "kind", nullable = false, length = 20)
  private String kind;

  @Column(name = "body", nullable = false, length = 800)
  private String body;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "delay_hours", nullable = false)
  private Integer delayHours;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public Long getBranchId() {
    return branchId;
  }

  public void setBranchId(Long value) {
    this.branchId = value;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String value) {
    this.kind = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    this.body = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
  }

  public Integer getDelayHours() {
    return delayHours;
  }

  public void setDelayHours(Integer value) {
    this.delayHours = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    this.version = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }
}
