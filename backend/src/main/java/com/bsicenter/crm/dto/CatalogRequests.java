package com.bsicenter.crm.dto;

import com.bsicenter.crm.validation.CustomerIdentity;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class CatalogRequests {
  private CatalogRequests() {}

  public record Customer(
      @NotBlank @Size(max = 100) String firstName,
      @NotBlank @Size(max = 100) String lastName,
      @Pattern(regexp = "09[0-9]{9}", message = "موبایل باید ۱۱ رقم و با 09 شروع شود") @NotNull
          String mobile,
      @Pattern(regexp = "09[0-9]{9}", message = "موبایل جایگزین معتبر نیست") String alternateMobile,
      @Pattern(regexp = "[0-9]{10}", message = "کد ملی باید ۱۰ رقم باشد") String nationalCode,
      @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED") String status,
      @Pattern(regexp = "SMS|PHONE|WHATSAPP") String contactChannel,
      LocalDate birthDate,
      @Size(max = 20) String gender,
      @Size(max = 30) String maritalStatus,
      @Size(max = 120) String education,
      @Size(max = 120) String occupation,
      @Size(max = 500) String address,
      @Size(max = 100) String city,
      @Size(max = 60) String acquisitionChannel,
      Long referrerId,
      Long preferredStaffId,
      boolean marketingConsent,
      @Size(max = 2000) String notes,
      boolean active) {
    public Customer {
      mobile = CustomerIdentity.phone(mobile);
      alternateMobile = CustomerIdentity.phone(alternateMobile);
      nationalCode = CustomerIdentity.nationalCode(nationalCode);
    }
  }

  public record Relation(
      @NotNull @Pattern(regexp = "SPOUSE|CHILD|OTHER") String relationship,
      @NotBlank @Size(max = 160) String fullName,
      LocalDate birthDate,
      @Size(max = 500) String notes) {}

  public record Preference(
      @NotBlank @Size(max = 60) String category, @NotBlank @Size(max = 500) String value) {}

  public record Occasion(@NotBlank @Size(max = 120) String title, @NotNull LocalDate date) {}

  public record Service(
      @NotBlank @Size(max = 160) String name,
      @NotBlank @Size(max = 80) String category,
      @Size(max = 500) String description,
      @NotNull @DecimalMin("0") BigDecimal price,
      @NotNull @DecimalMin("0") BigDecimal materialCost,
      @Min(5) @Max(480) int durationMinutes,
      @Min(0) @Max(120) int bufferMinutes,
      boolean active) {}

  public record Staff(
      @NotBlank @Size(max = 160) String name,
      @Size(max = 20) String mobile,
      @Size(max = 160) String specialty,
      @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal commissionRate,
      @NotNull LocalTime workStart,
      @NotNull LocalTime workEnd,
      @Pattern(regexp = "[1-7](,[1-7]){0,6}") @NotNull String workDays,
      boolean active,
      @NotNull List<Long> serviceIds) {}

  public record Leave(
      @NotNull OffsetDateTime startsAt,
      @NotNull OffsetDateTime endsAt,
      @Size(max = 300) String reason) {}

  public record Product(
      @NotBlank @Size(max = 160) String name,
      @NotBlank @Size(max = 80) String category,
      @NotNull @DecimalMin("0") BigDecimal price,
      @NotNull @DecimalMin("0") BigDecimal cost,
      @Min(0) int minimumStock,
      @Size(max = 160) String supplier,
      boolean active) {}

  public record Stock(
      @Min(-100000) @Max(100000) int quantity, @NotBlank @Size(max = 300) String reason) {}

  public record User(
      @NotBlank @Size(max = 80) String username,
      @Size(min = 12, max = 72) @NotNull String password,
      @NotBlank @Size(max = 160) String name,
      @Pattern(regexp = "ADMIN|MANAGER|RECEPTION|STYLIST|MARKETING|FINANCE") @NotNull String role,
      @NotNull Long branchId,
      Long staffId) {}

  public record UserPassword(@NotBlank @Size(min=12, max=72) String password) {}

  public record Name(@NotBlank @Size(max = 120) String name) {}

  public record Settings(
      @NotBlank @Size(max = 160) String organization,
      @Pattern(regexp = "BASIC|STANDARD|ADVANCED|SMART") @NotNull String plan,
      LocalDate expiresOn,
      @NotNull @DecimalMin("1") BigDecimal pointUnit,
      @Min(1) @Max(730) int riskDays,
      @Min(2) @Max(100) int loyalVisits) {}
}
