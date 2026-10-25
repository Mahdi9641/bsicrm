package com.bsicenter.crm.controller;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class FallbackErrorController implements ErrorController {
  @RequestMapping("/error") public ResponseEntity<?> error(HttpServletRequest request){
    Object value=request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int status=value instanceof Integer i && i>=400&&i<=599?i:500;
    String message=switch(status){case 400->"درخواست معتبر نیست";case 401->"برای ادامه وارد حساب شوید";case 403->"اجازهٔ دسترسی ندارید";case 404->"مسیر پیدا نشد";case 405->"روش درخواست مجاز نیست";case 415->"نوع محتوا پشتیبانی نمی‌شود";case 429->"تعداد درخواست‌ها زیاد است؛ کمی صبر کنید";default->"خطایی در پردازش درخواست رخ داد";};
    return ResponseEntity.status(status).body(Map.of("status",status,"code","REQUEST_ERROR","message",message));
  }
}
