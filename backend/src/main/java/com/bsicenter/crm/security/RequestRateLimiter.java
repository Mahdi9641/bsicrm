package com.bsicenter.crm.security;
import com.bsicenter.crm.service.AuthService;
import com.github.benmanes.caffeine.cache.*;
import io.github.bucket4j.*;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
@Service
public class RequestRateLimiter {
  private final RateLimitProperties properties;
  private final LettuceBasedProxyManager<String> redis;
  // Longer than the longest refill period, with bounded memory.
  private final Cache<String,Bucket> local=Caffeine.newBuilder().maximumSize(100_000).expireAfterAccess(Duration.ofMinutes(20)).build();
  private final AtomicBoolean failed=new AtomicBoolean();
  public RequestRateLimiter(RateLimitProperties properties,ObjectProvider<LettuceBasedProxyManager<String>> provider) {
    this.properties=properties; this.redis=properties.redisEnabled()?provider.getIfAvailable():null;
  }
  public ConsumptionProbe consume(String group,String identity) {
    // Include configuration so changed quotas take effect after a restart,
    // even when an older Redis bucket is still being used by another instance.
    String key="bsicrm:rate:v1:"+group+":"+properties.capacity(group)+":"+properties.period(group).toSeconds()+":"+AuthService.hash(identity);
    var config=BucketConfiguration.builder().addLimit(l->l.capacity(properties.capacity(group)).refillGreedy(properties.capacity(group),properties.period(group))).build();
    if(redis!=null) try {
      var result=redis.getProxy(key,()->config).tryConsumeAndReturnRemaining(1); failed.set(false); return result;
    } catch(RuntimeException e) {
      if(failed.compareAndSet(false,true)) LoggerFactory.getLogger(RequestRateLimiter.class).warn("ارتباط محدودساز با ردیس قطع شد؛ محدودسازی محلی تا بازیابی ارتباط فعال است");
    }
    return local.get(key,k->Bucket.builder().addLimit(l->l.capacity(properties.capacity(group)).refillGreedy(properties.capacity(group),properties.period(group))).build()).tryConsumeAndReturnRemaining(1);
  }
}
