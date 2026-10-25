package com.bsicenter.crm.config;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class PersianCorsConfig {
  @Bean CorsFilter corsFilter(CorsConfigurationSource source) {
    var filter=new CorsFilter(source);
    filter.setCorsProcessor(new DefaultCorsProcessor(){
      @Override protected void rejectRequest(ServerHttpResponse response)throws IOException{
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().set("Content-Type","application/json;charset=UTF-8");
        response.getBody().write("{\"status\":403,\"code\":\"CORS_DENIED\",\"message\":\"مبدأ یا مشخصات درخواست مجاز نیست\"}".getBytes(StandardCharsets.UTF_8));
        response.flush();
      }
    });return filter;
  }
  // Spring Security owns the filter. Do not run it a second time as a servlet filter.
  @Bean FilterRegistrationBean<CorsFilter> corsRegistration(CorsFilter filter){
    var registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;
  }
}
