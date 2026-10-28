package com.bsicenter.crm;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bsicenter.crm.security.*;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.*;
import tools.jackson.databind.ObjectMapper;
class RateLimitTest {
  RateLimitProperties props(boolean enabled){return new RateLimitProperties(enabled,false,"redis://unreachable:6379",10,3,2,4,5,6);}
  @SuppressWarnings("unchecked") RequestRateLimiter local(RateLimitProperties p){
    ObjectProvider<LettuceBasedProxyManager<String>> provider=mock(ObjectProvider.class);
    var limiter=new RequestRateLimiter(p,provider);verifyNoInteractions(provider);return limiter;
  }
  @Test void limitsAreAtomicAndIndependentPerUserAndOperation() throws Exception {
    var limiter=local(props(true));var pool=Executors.newFixedThreadPool(8);
    try {var tasks=IntStream.range(0,40).<Callable<Boolean>>mapToObj(i->()->limiter.consume("read","user:1").isConsumed()).toList();
      long accepted=0;for(var f:pool.invokeAll(tasks))if(f.get())accepted++;assertEquals(10,accepted);
    }finally{pool.shutdownNow();}
    assertTrue(limiter.consume("write","user:1").isConsumed());assertTrue(limiter.consume("read","user:2").isConsumed());
  }
  @Test void responseHasPersianMessageRetryHeaderAndNoControllerExecution()throws Exception{
    var p=props(true);var filter=new RateLimitFilter(local(p),p,new ObjectMapper());
    for(int i=0;i<3;i++){
      var req=new MockHttpServletRequest("POST","/api/auth/login");req.setRemoteAddr("127.0.0.8");req.addHeader("X-Forwarded-For","192.0.2."+i);
      var res=new MockHttpServletResponse();var chain=new MockFilterChain();filter.doFilter(req,res,chain);
      if(i<2)assertNotNull(chain.getRequest());else{assertEquals(429,res.getStatus());assertNull(chain.getRequest());assertTrue(Integer.parseInt(res.getHeader("Retry-After"))>0);assertTrue(res.getContentAsString().contains("تعداد درخواست‌ها"));}
    }
  }
  @Test void staticAssetsHealthAndPreflightDoNotConsumeTokens()throws Exception{
    var limiter=mock(RequestRateLimiter.class);var filter=new RateLimitFilter(limiter,props(true),new ObjectMapper());
    for(String path:new String[]{"/_next/static/app.js","/actuator/health","/api/customers"})filter.doFilter(new MockHttpServletRequest(path.startsWith("/api")?"OPTIONS":"GET",path),new MockHttpServletResponse(),new MockFilterChain());
    verifyNoInteractions(limiter);
  }
  @Test void disabledLimiterDoesNotConsumeTokens()throws Exception{
    var limiter=mock(RequestRateLimiter.class);new RateLimitFilter(limiter,props(false),new ObjectMapper()).doFilter(new MockHttpServletRequest("GET","/api/customers"),new MockHttpServletResponse(),new MockFilterChain());verifyNoInteractions(limiter);
  }
  @Test void authenticationAndRefreshHaveSeparateBudgets(){
    assertEquals("auth",RateLimitFilter.group("/api/auth/customer/register","POST",false));assertEquals("session",RateLimitFilter.group("/api/auth/refresh","POST",false));assertEquals("captcha",RateLimitFilter.group("/api/auth/captcha","GET",false));assertEquals("read",RateLimitFilter.group("/api/customers","GET",true));assertEquals("write",RateLimitFilter.group("/api/customers","POST",true));
  }
}
