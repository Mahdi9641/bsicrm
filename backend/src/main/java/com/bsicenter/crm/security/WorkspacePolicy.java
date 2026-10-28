package com.bsicenter.crm.security;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** One backend decision for every API request, independent of UI visibility. */
@Component
public class WorkspacePolicy implements HandlerInterceptor, WebMvcConfigurer {
  private final Access access;
  public WorkspacePolicy(Access access) { this.access=access; }
  @Override public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this).addPathPatterns("/api/**");
  }
  @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
    if(req.getMethod().equals("OPTIONS"))return true;
    String path=req.getRequestURI();
    String required=EndpointPermissions.required(req.getMethod(),path);
    if(required==null){
      if(!(handler instanceof org.springframework.web.method.HandlerMethod))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"مسیر درخواست پیدا نشد");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"این مسیر یا روش درخواست مجاز نیست");
    }
    if(required.equals("PUBLIC"))return true;
    access.user();
    if(access.customer()&&!path.startsWith("/api/portal/")&&!path.startsWith("/api/auth/")&&!path.equals("/api/access/me")&&!path.equals("/api/bootstrap"))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"این بخش برای حساب مشتری مجاز نیست");
    if(required.equals("IAM_READ"))access.anyPermission("users.read","roles.read");
    else if(required.startsWith("GROWTH.")){
      String type=path.split("/")[3];
      if(!java.util.Set.of("campaigns","events").contains(type))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"نوع گزارش مجاز نیست");
      access.permission(type+required.substring(6));
      if(path.endsWith("/costs"))access.permission("finance.write");
    }else if(!java.util.Set.of("SELF","LOOKUPS").contains(required))access.permission(required);
    if(!path.equals("/api/bootstrap")&&!path.startsWith("/api/auth/")&&!path.startsWith("/api/access/")&&!path.startsWith("/api/billing/")&&!path.startsWith("/api/tenancy/"))access.requireSubscription();
    return true;
  }
}
