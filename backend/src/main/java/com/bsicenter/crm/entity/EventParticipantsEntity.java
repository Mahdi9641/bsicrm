package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/**
 * Persistence mapping for event_participants. API responses use DTOs, never entity serialization.
 */
@Entity
@Table(name = "event_participants")
@IdClass(EventParticipantsEntity.Key.class)
public class EventParticipantsEntity {
  public EventParticipantsEntity() {}

  @Id
  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Id
  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private EventsEntity eventReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity customerReference;

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long value) {
    this.eventId = value;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public EventsEntity getEventReference() {
    return eventReference;
  }

  public CustomersEntity getCustomerReference() {
    return customerReference;
  }

  public static class Key implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public Long customerId;
    public Long eventId;

    public Key() {}

    @Override
    public boolean equals(Object other) {
      return other instanceof Key k
          && Objects.equals(customerId, k.customerId)
          && Objects.equals(eventId, k.eventId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(customerId, eventId);
    }
  }
}
