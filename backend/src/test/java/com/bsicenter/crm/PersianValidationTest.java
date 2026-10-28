package com.bsicenter.crm;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.Validation;
import jakarta.validation.constraints.*;
import java.util.Locale;
import org.junit.jupiter.api.Test;
class PersianValidationTest {
  record Form(@NotBlank String name,@Min(1) int quantity,@Size(min=12) String password,@Pattern(regexp="09[0-9]{9}") String phone){}
  @Test void defaultConstraintMessagesStayPersianEvenWithEnglishLocale(){
    var original=Locale.getDefault();try{Locale.setDefault(Locale.ENGLISH);try(var factory=Validation.buildDefaultValidatorFactory()){
      var errors=factory.getValidator().validate(new Form("",0,"short","invalid"));assertEquals(4,errors.size());errors.forEach(e->assertTrue(e.getMessage().matches("(?s).*[\\u0600-\\u06ff].*"),e.getMessage()));
    }}finally{Locale.setDefault(original);}
  }
}
