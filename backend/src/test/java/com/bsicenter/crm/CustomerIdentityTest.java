package com.bsicenter.crm;

import static org.junit.jupiter.api.Assertions.*;

import com.bsicenter.crm.validation.CustomerIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CustomerIdentityTest {
  @Test
  void normalizesPersianAndInternationalPhone() {
    assertEquals("09123456789", CustomerIdentity.phone(" +۹۸ (۹۱۲) ۳۴۵-۶۷۸۹ "));
    assertEquals("09123456789", CustomerIdentity.phone("0098 912 345 6789"));
  }

  @Test
  void preservesLeadingZeroInNationalCode() {
    assertEquals("0012345679", CustomerIdentity.nationalCode("۰۰۱۲۳۴۵۶۷۹"));
    assertDoesNotThrow(() -> CustomerIdentity.validateNationalCode("0012345679"));
  }

  @Test
  void rejectsWrongChecksumAndRepeatedDigits() {
    assertThrows(
        ResponseStatusException.class, () -> CustomerIdentity.validateNationalCode("0012345678"));
    assertThrows(
        ResponseStatusException.class, () -> CustomerIdentity.validateNationalCode("1111111111"));
  }

  @Test
  void permitsMissingOptionalIdentity() {
    assertNull(CustomerIdentity.nationalCode(" "));
    assertNull(CustomerIdentity.phone(" "));
    assertDoesNotThrow(() -> CustomerIdentity.validateNationalCode(null));
  }
}
