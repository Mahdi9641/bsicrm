package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for messages. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "messages")
public class MessagesEntity {
  public MessagesEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "campaign_id", nullable = true)
  private Long campaignId;

  @Column(name = "kind", nullable = false, length = 20)
  private String kind;

  @Column(name = "body", nullable = false, length = 1000)
  private String body;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "provider_id", nullable = true, length = 120)
  private String providerId;

  @Column(name = "error", nullable = true, length = 300)
  private String error;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "sent_at", nullable = true)
  private OffsetDateTime sentAt;

  @Column(name = "dedupe_key", nullable = true, length = 180)
  private String dedupeKey;

  @Column(name = "appointment_id", nullable = true)
  private Long appointmentId;

  @Column(name = "appointment_version", nullable = true)
  private Long appointmentVersion;

  @Column(name = "invoice_id", nullable = true)
  private Long invoiceId;

  @Column(name = "scheduled_at", nullable = false)
  private OffsetDateTime scheduledAt;

  @Column(name = "delivery_status", nullable = false, length = 20)
  private String deliveryStatus;

  @Column(name = "delivered_at", nullable = true)
  private OffsetDateTime deliveredAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CampaignsEntity campaignReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppointmentsEntity appointmentReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private InvoicesEntity invoiceReference;

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

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public Long getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(Long value) {
    this.campaignId = value;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String value) {
    this.providerId = value;
  }

  public String getError() {
    return error;
  }

  public void setError(String value) {
    this.error = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public OffsetDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(OffsetDateTime value) {
    this.sentAt = value;
  }

  public String getDedupeKey() {
    return dedupeKey;
  }

  public void setDedupeKey(String value) {
    this.dedupeKey = value;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long value) {
    this.appointmentId = value;
  }

  public Long getAppointmentVersion() {
    return appointmentVersion;
  }

  public void setAppointmentVersion(Long value) {
    this.appointmentVersion = value;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(Long value) {
    this.invoiceId = value;
  }

  public OffsetDateTime getScheduledAt() {
    return scheduledAt;
  }

  public void setScheduledAt(OffsetDateTime value) {
    this.scheduledAt = value;
  }

  public String getDeliveryStatus() {
    return deliveryStatus;
  }

  public void setDeliveryStatus(String value) {
    this.deliveryStatus = value;
  }

  public OffsetDateTime getDeliveredAt() {
    return deliveredAt;
  }

  public void setDeliveredAt(OffsetDateTime value) {
    this.deliveredAt = value;
  }

  public BranchesEntity getBranchReference() {
    return branchReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public CampaignsEntity getCampaignReference() {
    return campaignReference;
  }

  public AppointmentsEntity getAppointmentReference() {
    return appointmentReference;
  }

  public InvoicesEntity getInvoiceReference() {
    return invoiceReference;
  }
}
