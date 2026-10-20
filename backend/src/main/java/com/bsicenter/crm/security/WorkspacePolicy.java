package com.bsicenter.crm.security;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Server-side policy applies to reads AND writes, regardless of browser navigation. */
@Component
public class WorkspacePolicy implements HandlerInterceptor, WebMvcConfigurer {
  private final Access access;
  public WorkspacePolicy(Access access) { this.access=access; }
  @Override public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this).addPathPatterns("/api/**")
      .excludePathPatterns("/api/auth/**", "/api/public/invoices/**", "/api/integrations/sms/delivery", "/api/billing/callback");
  }
  @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
    if (req.getMethod().equals("OPTIONS")) return true;
    String path=req.getRequestURI();
    var user=access.user();
    if (access.customer() && !path.startsWith("/api/portal/") && !path.equals("/api/bootstrap"))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"این بخش برای حساب مشتری مجاز نیست");
    if (path.equals("/api/bootstrap") || path.startsWith("/api/tenancy/") || path.equals("/api/tenancy") || path.startsWith("/api/billing/")) return true;
    String root=path.substring("/api/".length()).split("/")[0];
    switch(root){
      case "appointments", "visits", "visit-services" -> access.roles("ADMIN","MANAGER","RECEPTION","STYLIST");
      case "customers" -> access.roles("ADMIN","MANAGER","RECEPTION","MARKETING","FINANCE");
      case "finance", "reports", "accounts", "transfers", "expenses" -> access.finance();
      case "invoices", "payments", "products" -> access.sales();
      case "campaigns", "events", "messages", "templates", "growth", "rfm" -> access.marketing();
      case "loyalty", "tasks" -> access.roles("ADMIN","MANAGER","RECEPTION","MARKETING");
      case "audit" -> access.roles("ADMIN","MANAGER");
      case "settings", "branches" -> access.roles("ADMIN");
      case "users" -> {if(req.getMethod().equals("POST") && path.equals("/api/users")) access.roles("ADMIN","MANAGER"); else access.roles("ADMIN");}
      case "commissions" -> access.roles("ADMIN","MANAGER","FINANCE","STYLIST");
      case "commission-rules" -> access.finance();
      default -> {} // Lookup and specialized services apply their narrower operation policies.
    }
    access.requireSubscription();
    return true;
  }
}
