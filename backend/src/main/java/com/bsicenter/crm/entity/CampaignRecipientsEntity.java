package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for campaign_recipients. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "campaign_recipients")
@IdClass(CampaignRecipientsEntity.Key.class)
public class CampaignRecipientsEntity {
  public CampaignRecipientsEntity() {}

  @Id
  @Column(name = "campaign_id", nullable = false)
  private Long campaignId;

  @Id
  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "message_id", nullable = true)
  private Long messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CampaignsEntity campaignReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private MessagesEntity messageReference;

  public Long getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(Long value) {
    this.campaignId = value;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public Long getMessageId() {
    return messageId;
  }

  public void setMessageId(Long value) {
    this.messageId = value;
  }

  public CampaignsEntity getCampaignReference() {
    return campaignReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public MessagesEntity getMessageReference() {
    return messageReference;
  }

  public static class Key implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public Long customerId;
    public Long campaignId;

    public Key() {}

    @Override
    public boolean equals(Object other) {
      return other instanceof Key k
          && Objects.equals(customerId, k.customerId)
          && Objects.equals(campaignId, k.campaignId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(customerId, campaignId);
    }
  }
}
