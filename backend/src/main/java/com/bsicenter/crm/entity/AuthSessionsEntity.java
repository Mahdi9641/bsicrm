package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for auth_sessions. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "auth_sessions")
public class AuthSessionsEntity {
  public AuthSessionsEntity() {}

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "revoked", nullable = false)
  private Boolean revoked;

  @Column(name = "user_agent", nullable = false, length = 300)
  private String userAgent;

  @Column(name = "remote_address", nullable = false, length = 64)
  private String remoteAddress;

  @Column(name = "refreshed_at", nullable = false)
  private OffsetDateTime refreshedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private AppUsersEntity userReference;

  public UUID getId() {
    return id;
  }

  public void setId(UUID value) {
    this.id = value;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long value) {
    this.userId = value;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String value) {
    this.tokenHash = value;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime value) {
    this.expiresAt = value;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime value) {
    this.createdAt = value;
  }

  public Boolean getRevoked() {
    return revoked;
  }

  public void setRevoked(Boolean value) {
    this.revoked = value;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String value) {
    this.userAgent = value;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(String value) {
    this.remoteAddress = value;
  }

  public OffsetDateTime getRefreshedAt() {
    return refreshedAt;
  }

  public void setRefreshedAt(OffsetDateTime value) {
    this.refreshedAt = value;
  }

  public AppUsersEntity getUserReference() {
    return userReference;
  }
}
