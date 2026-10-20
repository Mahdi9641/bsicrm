package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.AuthRequests.*;
import com.bsicenter.crm.service.AuthService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @GetMapping("/captcha")
  public Object captcha(HttpServletRequest req,@RequestParam(defaultValue="puzzle") String mode) { return service.captcha(req,mode); }

  @GetMapping("/salons") public Object salons() { return service.salons(); }

  @PostMapping("/login")
  public Object login(
      @Valid @RequestBody Login body, HttpServletRequest req, HttpServletResponse res) {
    return service.login(body, req, res);
  }

  @PostMapping("/customer/register")
  public Object register(@Valid @RequestBody Register body, HttpServletRequest req, HttpServletResponse res) { return service.register(body, req, res); }

  @PostMapping("/customer/login")
  public Object customerLogin(@Valid @RequestBody CustomerLogin body, HttpServletRequest req, HttpServletResponse res) { return service.customerLogin(body, req, res); }

  @PostMapping("/refresh")
  public Object refresh(
      @CookieValue(name = "bsi_refresh", defaultValue = "") String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    return service.refresh(token, req, res);
  }

  @PostMapping("/logout")
  public Object logout(
      @CookieValue(name = "bsi_refresh", defaultValue = "") String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    return service.logout(token, req, res);
  }

  @GetMapping("/me")
  public Object me() {
    return service.me();
  }

  @PostMapping("/password")
  public Object password(@Valid @RequestBody Password b) {
    return service.password(b);
  }

  @GetMapping("/sessions")
  public Object sessions() {
    return service.sessions();
  }

  @DeleteMapping("/sessions/{id}")
  public Object revoke(@PathVariable UUID id) {
    return service.revoke(id);
  }
}
