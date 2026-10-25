package com.bsicenter.crm.config;

import com.bsicenter.crm.security.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public SecretKeySpec jwtKey(@Value("${app.jwt-secret}") String secret) {
    if (secret.length() < 48)
      throw new IllegalArgumentException("کلید JWT_SECRET باید دست‌کم ۴۸ نویسهٔ تصادفی داشته باشد");
    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKeySpec key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKeySpec key) {
    var d = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer("bsicrm"));
    return d;
  }

  @Bean
  @Primary
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors-origins}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOriginPatterns(Arrays.asList(origins.split(",")));
    c.setAllowCredentials(true);
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
    c.setMaxAge(3600L);
    c.setExposedHeaders(List.of("X-Request-ID", "Retry-After", "X-RateLimit-Remaining"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      RequestRateLimiter limiter, RateLimitProperties rateProperties,
      tools.jackson.databind.ObjectMapper mapper)
      throws Exception {
    return http.cors(c -> {})
        .csrf(c -> c.disable())
        .addFilterAfter(new RateLimitFilter(limiter,rateProperties,mapper),BearerTokenAuthenticationFilter.class)
        .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{
          res.setStatus(401);res.setContentType("application/json;charset=UTF-8");
          mapper.writeValue(res.getWriter(),Map.of("status",401,"code","AUTH_REQUIRED","message","برای ادامه وارد حساب شوید"));
        }).accessDeniedHandler((req,res,ex)->{
          res.setStatus(403);res.setContentType("application/json;charset=UTF-8");
          mapper.writeValue(res.getWriter(),Map.of("status",403,"code","ACCESS_DENIED","message","اجازهٔ دسترسی به این بخش را ندارید"));
        }))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/auth/**",
                        "/error",
                        "/api/billing/callback",
                        "/api/public/invoices/**",
                        "/api/integrations/sms/delivery",
                        "/actuator/health",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> {})
                    .authenticationEntryPoint(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          mapper.writeValue(
                              res.getWriter(),
                              Map.of(
                                  "status",
                                  401,
                                  "code",
                                  "AUTH_REQUIRED",
                                  "message",
                                  "نشست معتبر نیست؛ دوباره وارد شوید",
                                  "requestId",
                                  Objects.toString(
                                      req.getAttribute(RequestContextFilter.ATTRIBUTE), "")));
                        }))
        .build();
  }
}
