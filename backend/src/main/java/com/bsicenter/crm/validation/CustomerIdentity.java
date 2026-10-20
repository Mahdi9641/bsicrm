package com.bsicenter.crm.validation;

import com.bsicenter.crm.exception.ApiError;

public final class CustomerIdentity {
  private CustomerIdentity() {}

  public static String digits(String value) {
    if (value == null) return null;
    StringBuilder result = new StringBuilder();
    for (char c : value.trim().toCharArray()) {
      if (c >= '۰' && c <= '۹') result.append((char) ('0' + c - '۰'));
      else if (c >= '٠' && c <= '٩') result.append((char) ('0' + c - '٠'));
      else result.append(c);
    }
    return result.toString();
  }

  public static String phone(String value) {
    String v = digits(value);
    if (v == null || v.isBlank()) return null;
    v = v.replaceAll("[\\s()\\-]", "");
    if (v.startsWith("+98")) v = "0" + v.substring(3);
    else if (v.startsWith("0098")) v = "0" + v.substring(4);
    return v;
  }

  public static String nationalCode(String value) {
    String v = digits(value);
    if (v == null || v.isBlank()) return null;
    return v;
  }

  public static void validateNationalCode(String value) {
    if (value == null) return;
    if (!value.matches("[0-9]{10}") || value.chars().distinct().count() == 1)
      throw ApiError.bad("کد ملی باید ۱۰ رقم معتبر باشد");
    int sum = 0;
    for (int i = 0; i < 9; i++) sum += (value.charAt(i) - '0') * (10 - i);
    int remainder = sum % 11;
    int expected = remainder < 2 ? remainder : 11 - remainder;
    if (value.charAt(9) - '0' != expected) throw ApiError.bad("رقم کنترل کد ملی معتبر نیست");
  }
}
