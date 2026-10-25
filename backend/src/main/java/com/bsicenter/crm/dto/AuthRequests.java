package com.bsicenter.crm.dto;

import jakarta.servlet.http.*;
import jakarta.validation.constraints.*;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

public final class AuthRequests {
  private AuthRequests() {}

  public record Login(
      @NotBlank @Size(max = 80) String username, @NotBlank @Size(max = 100) String password,
      @NotBlank @Size(max = 80) String captchaId, @NotBlank @Size(max = 10) String captchaAnswer) {}

  public record Register(
      @NotNull @Positive Long branchId,
      @NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName,
      @NotNull @Pattern(regexp = "09[0-9]{9}") String mobile,
      @NotBlank @Size(min = 12, max = 72) String password,
      @NotBlank @Size(max = 80) String captchaId, @NotBlank @Size(max = 10) String captchaAnswer) {}

  public record CustomerLogin(
      @NotNull @Positive Long branchId,
      @NotBlank @Size(max = 80) String mobile, @NotBlank @Size(max = 72) String password,
      @NotBlank @Size(max = 80) String captchaId, @NotBlank @Size(max = 10) String captchaAnswer) {}

  public record Password(
      @NotBlank String currentPassword, @NotBlank @Size(min = 12, max = 72) String newPassword) {}
}
