package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for growth_costs. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "growth_costs")
public class GrowthCostsEntity {
  public GrowthCostsEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "campaign_id", nullable = true)
  private Long campaignId;

  @Column(name = "event_id", nullable = true)
  private Long eventId;

  @Column(name = "expense_id", nullable = false)
  private Long expenseId;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CampaignsEntity campaignReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private EventsEntity eventReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "expense_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private ExpensesEntity expenseReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity createdByReference;

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

  public Long getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(Long value) {
    this.campaignId = value;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long value) {
    this.eventId = value;
  }

  public Long getExpenseId() {
    return expenseId;
  }

  public void setExpenseId(Long value) {
    this.expenseId = value;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long value) {
    this.createdBy = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public CampaignsEntity getCampaignReference() {
    return campaignReference;
  }

  public EventsEntity getEventReference() {
    return eventReference;
  }

  public ExpensesEntity getExpenseReference() {
    return expenseReference;
  }

  public AppUsersEntity getCreatedByReference() {
    return createdByReference;
  }
}
