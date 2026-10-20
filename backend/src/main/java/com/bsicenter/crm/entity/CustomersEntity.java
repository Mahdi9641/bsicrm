package com.bsicenter.crm.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Persistence mapping for customers. API responses use DTOs, never entity serialization. */
@Entity
@Table(name = "customers")
public class CustomersEntity {
  public CustomersEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "branch_id", nullable = false)
  private Long branchId;

  @Column(name = "national_code", length = 10)
  private String nationalCode;

  @Column(name = "customer_code", length = 40, insertable = false, updatable = false)
  private String customerCode;

  @Column(name = "status", length = 20, nullable = false)
  private String status;

  public String getNationalCode() {
    return nationalCode;
  }

  public void setNationalCode(String value) {
    nationalCode = value;
  }

  public String getCustomerCode() {
    return customerCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    status = value;
  }

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "mobile", nullable = false, length = 20)
  private String mobile;

  @Column(name = "alternate_mobile", nullable = true, length = 20)
  private String alternateMobile;

  @Column(name = "birth_date", nullable = true)
  private LocalDate birthDate;

  @Column(name = "gender", nullable = true, length = 20)
  private String gender;

  @Column(name = "marital_status", nullable = true, length = 30)
  private String maritalStatus;

  @Column(name = "education", nullable = true, length = 120)
  private String education;

  @Column(name = "occupation", nullable = true, length = 120)
  private String occupation;

  @Column(name = "address", nullable = true, length = 500)
  private String address;

  @Column(name = "city", nullable = true, length = 100)
  private String city;

  @Column(name = "acquisition_channel", nullable = false, length = 60)
  private String acquisitionChannel;

  @Column(name = "referrer_id", nullable = true)
  private Long referrerId;

  @Column(name = "preferred_staff_id", nullable = true)
  private Long preferredStaffId;

  @Column(name = "contact_channel", nullable = false, length = 30)
  private String contactChannel;

  @Column(name = "marketing_consent", nullable = false)
  private Boolean marketingConsent;

  @Column(name = "consent_at", nullable = true)
  private OffsetDateTime consentAt;

  @Column(name = "notes", nullable = true, length = 2000)
  private String notes;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private BranchesEntity branchReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referrer_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomersEntity referrerReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "preferred_staff_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private StaffEntity preferredStaffReference;

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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String value) {
    this.firstName = value;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String value) {
    this.lastName = value;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String value) {
    this.mobile = value;
  }

  public String getAlternateMobile() {
    return alternateMobile;
  }

  public void setAlternateMobile(String value) {
    this.alternateMobile = value;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate value) {
    this.birthDate = value;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String value) {
    this.gender = value;
  }

  public String getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(String value) {
    this.maritalStatus = value;
  }

  public String getEducation() {
    return education;
  }

  public void setEducation(String value) {
    this.education = value;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String value) {
    this.occupation = value;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String value) {
    this.city = value;
  }

  public String getAcquisitionChannel() {
    return acquisitionChannel;
  }

  public void setAcquisitionChannel(String value) {
    this.acquisitionChannel = value;
  }

  public Long getReferrerId() {
    return referrerId;
  }

  public void setReferrerId(Long value) {
    this.referrerId = value;
  }

  public Long getPreferredStaffId() {
    return preferredStaffId;
  }

  public void setPreferredStaffId(Long value) {
    this.preferredStaffId = value;
  }

  public String getContactChannel() {
    return contactChannel;
  }

  public void setContactChannel(String value) {
    this.contactChannel = value;
  }

  public Boolean getMarketingConsent() {
    return marketingConsent;
  }

  public void setMarketingConsent(Boolean value) {
    this.marketingConsent = value;
  }

  public OffsetDateTime getConsentAt() {
    return consentAt;
  }

  public void setConsentAt(OffsetDateTime value) {
    this.consentAt = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean value) {
    this.active = value;
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

  public CustomersEntity getReferrerReference() {
    return referrerReference;
  }

  public StaffEntity getPreferredStaffReference() {
    return preferredStaffReference;
  }
}
