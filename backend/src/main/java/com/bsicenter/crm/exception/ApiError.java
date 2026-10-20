package com.bsicenter.crm.exception;

import com.bsicenter.crm.security.RequestContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiError {
  public static ResponseStatusException bad(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }

  public static ResponseStatusException missing() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "رکورد پیدا نشد");
  }

  public static ResponseStatusException conflict(String message) {
    return new ResponseStatusException(HttpStatus.CONFLICT, message);
  }

  private ResponseEntity<Map<String, Object>> response(
      int status, String code, String message, HttpServletRequest request, Object fields) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status);
    body.put("code", code);
    body.put("message", message);
    body.put(
        "requestId", Objects.toString(request.getAttribute(RequestContextFilter.ATTRIBUTE), ""));
    body.put("timestamp", Instant.now());
    if (fields != null) body.put("fields", fields);
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> status(ResponseStatusException e, HttpServletRequest request) {
    int status = e.getStatusCode().value();
    String code =
        switch (status) {
          case 401 -> "AUTH_REQUIRED";
          case 402 -> "SUBSCRIPTION_EXPIRED";
          case 403 -> "ACCESS_DENIED";
          case 404 -> "NOT_FOUND";
          case 409 -> "CONFLICT";
          default -> "BUSINESS_RULE";
        };
    return response(status, code, Objects.toString(e.getReason(), "خطا در درخواست"), request, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validation(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    var fields =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                f ->
                    Map.of(
                        "field",
                        f.getField(),
                        "message",
                        Objects.toString(f.getDefaultMessage(), "نامعتبر")))
            .toList();
    return response(400, "VALIDATION_FAILED", "اطلاعات فرم معتبر نیست", request, fields);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> integrity(
      DataIntegrityViolationException e, HttpServletRequest request) {
    String detail = Objects.toString(e.getMostSpecificCause().getMessage(), "");
    if (detail.contains("uq_customers_mobile_global")
        || detail.contains("customers_branch_id_mobile_key"))
      return response(
          409, "CUSTOMER_MOBILE_EXISTS", "شماره موبایل قبلاً ثبت شده است", request, null);
    if (detail.contains("uq_customers_national_code"))
      return response(
          409, "CUSTOMER_NATIONAL_CODE_EXISTS", "کد ملی قبلاً ثبت شده است", request, null);
    return response(
        409,
        "DATA_CONFLICT",
        "اطلاعات تکراری، وابستگی نامعتبر یا تداخل زمانی وجود دارد",
        request,
        null);
  }

  @ExceptionHandler(TransientDataAccessException.class)
  public ResponseEntity<?> concurrent(TransientDataAccessException e, HttpServletRequest request) {
    return response(
        409,
        "CONCURRENT_CHANGE",
        "اطلاعات هم‌زمان تغییر کرده است؛ صفحه را تازه کنید و دوباره تلاش کنید",
        request,
        null);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  public ResponseEntity<?> invalid(Exception e, HttpServletRequest request) {
    return response(400, "INVALID_REQUEST", "قالب یا مقدار داده نامعتبر است", request, null);
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> method(
      org.springframework.web.HttpRequestMethodNotSupportedException e,
      HttpServletRequest request) {
    return response(405, "METHOD_NOT_ALLOWED", "روش درخواست مجاز نیست", request, null);
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<?> resource(
      org.springframework.web.servlet.resource.NoResourceFoundException e,
      HttpServletRequest request) {
    return response(404, "NOT_FOUND", "مسیر پیدا نشد", request, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> unexpected(Exception e, HttpServletRequest request) {
    // No exception message/body in logs: SQL and user data may be embedded in them.
    LoggerFactory.getLogger(ApiError.class)
        .error(
            "Unhandled type={} requestId={} locations={}",
            e.getClass().getSimpleName(),
            request.getAttribute(RequestContextFilter.ATTRIBUTE),
            Arrays.stream(e.getStackTrace())
                .filter(frame -> frame.getClassName().startsWith("com.bsicenter.crm."))
                .limit(6)
                .toList());
    return response(
        500,
        "INTERNAL_ERROR",
        "خطای داخلی رخ داد؛ شناسه درخواست را به پشتیبانی بدهید",
        request,
        null);
  }
}
