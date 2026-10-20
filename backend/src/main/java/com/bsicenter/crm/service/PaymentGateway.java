package com.bsicenter.crm.service;

import com.bsicenter.crm.exception.ApiError;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/** Zarinpal only; no payment is accepted based on browser callback parameters. */
@Service
public class PaymentGateway {
  final String merchant,publicUrl;final boolean sandbox;final ObjectMapper mapper;
  final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
  public PaymentGateway(@Value("${app.payment-merchant:}") String merchant,@Value("${app.public-url}") String publicUrl,@Value("${app.payment-sandbox:false}") boolean sandbox,ObjectMapper mapper){this.merchant=merchant;this.publicUrl=publicUrl;this.sandbox=sandbox;this.mapper=mapper;}
  public boolean enabled(){return !merchant.isBlank();}
  private Map<String,Object> call(String action,Map<String,Object> payload){
    if(!enabled())throw ApiError.bad("درگاه آنلاین تنظیم نشده است؛ از پرداخت با تأیید مدیر استفاده کنید");
    try{
      var req=HttpRequest.newBuilder(URI.create("https://"+(sandbox?"sandbox":"api")+".zarinpal.com/pg/v4/payment/"+action+".json"))
        .timeout(Duration.ofSeconds(20)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
      var response=client.send(req,HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()!=200)throw new IllegalStateException();
      var body=mapper.readValue(response.body(),Map.class);
      if(!(body.get("data") instanceof Map<?,?>)) throw new IllegalStateException();
      return (Map<String,Object>)body.get("data");
    }catch(Exception e){throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,"پاسخ معتبر از درگاه دریافت نشد؛ وضعیت سفارش را دوباره بررسی کنید");}
  }
  public Map<String,Object> request(UUID order,BigDecimal toman){
    var d=call("request",Map.of("merchant_id",merchant,"amount",toman.multiply(BigDecimal.TEN).longValueExact(),"description","تمدید اشتراک BsiCRM "+order,"callback_url",publicUrl+"/api/billing/callback"));
    if(((Number)d.getOrDefault("code",0)).intValue()!=100 || d.get("authority")==null)throw ApiError.bad("درگاه ایجاد پرداخت را نپذیرفت");
    String authority=d.get("authority").toString();
    return Map.of("authority",authority,"url",paymentUrl(authority));
  }
  public String paymentUrl(String authority){return "https://"+(sandbox?"sandbox":"www")+".zarinpal.com/pg/StartPay/"+authority;}
  public String verify(String authority,BigDecimal toman){
    var d=call("verify",Map.of("merchant_id",merchant,"amount",toman.multiply(BigDecimal.TEN).longValueExact(),"authority",authority));
    int code=((Number)d.getOrDefault("code",0)).intValue();
    if((code!=100 && code!=101) || d.get("ref_id")==null)throw ApiError.bad("پرداخت توسط درگاه تأیید نشده است");
    return "ZARINPAL:"+d.get("ref_id");
  }
}
