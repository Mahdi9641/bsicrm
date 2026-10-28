package com.bsicenter.crm;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bsicenter.crm.security.*;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.*;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.ObjectProvider;
class RedisRateLimitTest {
  @Test @EnabledIfEnvironmentVariable(named="BSICRM_TEST_REDIS_URI",matches=".+") @SuppressWarnings("unchecked")
  void independentInstancesShareTheSameAtomicBucketAndKeysExpire(){
    var client=RedisClient.create(System.getenv("BSICRM_TEST_REDIS_URI"));
    try(var connection=client.connect(RedisCodec.of(StringCodec.UTF8,ByteArrayCodec.INSTANCE))){
      var manager=Bucket4jLettuce.casBasedBuilder(connection).expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(1))).build();
      ObjectProvider<LettuceBasedProxyManager<String>> provider=mock(ObjectProvider.class);when(provider.getIfAvailable()).thenReturn(manager);
      var props=new RateLimitProperties(true,true,System.getenv("BSICRM_TEST_REDIS_URI"),2,2,2,2,2,2);var first=new RequestRateLimiter(props,provider);var second=new RequestRateLimiter(props,provider);String id="test:"+UUID.randomUUID();
      assertTrue(first.consume("read",id).isConsumed());assertTrue(second.consume("read",id).isConsumed());assertFalse(first.consume("read",id).isConsumed());
      String key="bsicrm:rate:v1:read:2:60:"+com.bsicenter.crm.service.AuthService.hash(id);assertTrue(connection.sync().pttl(key)>0);
      var changed=new RequestRateLimiter(new RateLimitProperties(true,true,System.getenv("BSICRM_TEST_REDIS_URI"),3,2,2,2,2,2),provider);
      assertTrue(changed.consume("read",id).isConsumed());assertTrue(changed.consume("read",id).isConsumed());assertTrue(changed.consume("read",id).isConsumed());assertFalse(changed.consume("read",id).isConsumed());
      connection.sync().del(key,"bsicrm:rate:v1:read:3:60:"+com.bsicenter.crm.service.AuthService.hash(id));
    }finally{client.shutdown();}
  }
}
