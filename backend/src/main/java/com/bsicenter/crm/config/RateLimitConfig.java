package com.bsicenter.crm.config;
import com.bsicenter.crm.security.RateLimitProperties;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.*;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
  @Configuration
  @ConditionalOnProperty(name="app.rate-limit.redis-enabled", havingValue="true")
  static class RedisBuckets {
    @Bean(destroyMethod="shutdown") RedisClient rateLimitRedisClient(RateLimitProperties p) {
      var uri=RedisURI.create(p.redisUri()); uri.setTimeout(Duration.ofMillis(500)); return RedisClient.create(uri);
    }
    @Bean(destroyMethod="close") StatefulRedisConnection<String,byte[]> rateLimitRedisConnection(RedisClient client) {
      return client.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }
    @Bean LettuceBasedProxyManager<String> rateLimitProxyManager(StatefulRedisConnection<String,byte[]> connection) {
      return Bucket4jLettuce.casBasedBuilder(connection)
          .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1))).build();
    }
  }
}
