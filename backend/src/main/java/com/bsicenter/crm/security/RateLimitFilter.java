package com.bsicenter.crm.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
/** Installed once, after JWT verification, within the security chain. */
public class RateLimitFilter extends OncePerRequestFilter {
  private final RequestRateLimiter limiter; private final RateLimitProperties properties; private final ObjectMapper mapper;
  public RateLimitFilter(RequestRateLimiter limiter,RateLimitProperties properties,ObjectMapper mapper){this.limiter=limiter;this.properties=properties;this.mapper=mapper;}
  @Override protected boolean shouldNotFilter(HttpServletRequest r){return !properties.enabled() || !r.getRequestURI().startsWith("/api/") || r.getMethod().equals("OPTIONS");}
  public static String group(String path,String method,boolean authenticated){
    if(Set.of("/api/auth/login","/api/auth/customer/login","/api/auth/customer/register").contains(path))return "auth";
    if(path.equals("/api/auth/captcha"))return "captcha";
    if(Set.of("/api/auth/refresh","/api/auth/logout").contains(path))return "session";
    if(!authenticated)return "public";
    return method.equals("GET")||method.equals("HEAD")?"read":"write";
  }
  @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
    var auth=SecurityContextHolder.getContext().getAuthentication();
    Jwt jwt=auth!=null&&auth.isAuthenticated()&&auth.getPrincipal() instanceof Jwt j?j:null;
    String group=group(req.getRequestURI(),req.getMethod(),jwt!=null);
    // Never accept user-supplied forwarded headers as an identity.
    String identity=jwt!=null&&(group.equals("read")||group.equals("write"))?"user:"+jwt.getSubject():"ip:"+req.getRemoteAddr();
    var probe=limiter.consume(group,identity);
    res.setHeader("X-RateLimit-Remaining",Long.toString(probe.getRemainingTokens()));
    if(probe.isConsumed()){chain.doFilter(req,res);return;}
    long wait=Math.max(1,(probe.getNanosToWaitForRefill()+999_999_999L)/1_000_000_000L);
    res.setStatus(429);res.setHeader("Retry-After",Long.toString(wait));res.setHeader("Cache-Control","no-store");res.setContentType("application/json;charset=UTF-8");
    mapper.writeValue(res.getWriter(),Map.of("status",429,"code","RATE_LIMITED","message","تعداد درخواست‌ها زیاد است؛ کمی صبر کنید و دوباره تلاش کنید","retryAfterSeconds",wait,"requestId",Objects.toString(req.getAttribute(RequestContextFilter.ATTRIBUTE),"")));
  }
}
