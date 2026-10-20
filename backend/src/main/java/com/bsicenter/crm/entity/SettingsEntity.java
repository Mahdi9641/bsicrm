package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Persistence mapping for settings. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "settings")
public class SettingsEntity {
  public SettingsEntity() {}

  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "organization", nullable = false, length = 160)
  private String organization;

  @Column(name = "plan", nullable = false, length = 16)
  private String plan;

  @Column(name = "expires_on", nullable = true)
  private LocalDate expiresOn;

  @Column(name = "timezone", nullable = false, length = 60)
  private String timezone;

  @Column(name = "currency", nullable = false, length = 10)
  private String currency;

  @Column(name = "point_unit", nullable = false, precision = 16, scale = 0)
  private BigDecimal pointUnit;

  @Column(name = "risk_days", nullable = false)
  private Integer riskDays;

  @Column(name = "loyal_visits", nullable = false)
  private Integer loyalVisits;

  public Integer getId() {
    return id;
  }

  public void setId(Integer value) {
    this.id = value;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String value) {
    this.organization = value;
  }

  public String getPlan() {
    return plan;
  }

  public void setPlan(String value) {
    this.plan = value;
  }

  public LocalDate getExpiresOn() {
    return expiresOn;
  }

  public void setExpiresOn(LocalDate value) {
    this.expiresOn = value;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String value) {
    this.timezone = value;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String value) {
    this.currency = value;
  }

  public BigDecimal getPointUnit() {
    return pointUnit;
  }

  public void setPointUnit(BigDecimal value) {
    this.pointUnit = value;
  }

  public Integer getRiskDays() {
    return riskDays;
  }

  public void setRiskDays(Integer value) {
    this.riskDays = value;
  }

  public Integer getLoyalVisits() {
    return loyalVisits;
  }

  public void setLoyalVisits(Integer value) {
    this.loyalVisits = value;
  }
}
