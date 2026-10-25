package com.bsicenter.crm.security;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(boolean enabled, boolean redisEnabled, String redisUri,
    int readCapacity, int writeCapacity, int authCapacity, int captchaCapacity, int sessionCapacity, int publicCapacity) {
  public RateLimitProperties {
    if (readCapacity<1 || writeCapacity<1 || authCapacity<1 || captchaCapacity<1 || sessionCapacity<1 || publicCapacity<1)
      throw new IllegalArgumentException("ظرفیت محدودسازی درخواست باید بزرگ‌تر از صفر باشد");
  }
  public long capacity(String group) { return switch(group) {
    case "auth" -> authCapacity; case "captcha" -> captchaCapacity; case "session" -> sessionCapacity;
    case "read" -> readCapacity; case "write" -> writeCapacity; default -> publicCapacity; }; }
  public Duration period(String group) { return Duration.ofMinutes(group.equals("auth") || group.equals("captcha") ? 10 : 1); }
}
