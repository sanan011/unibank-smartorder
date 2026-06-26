package az.unibank.smartorder.order.infrastructure.ratelimit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> slidingWindowScript;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // Sliding window rate limit Lua script
        // ARGV[1] = current timestamp
        // ARGV[2] = window size in milliseconds
        // ARGV[3] = limit
        // returns [1 (allowed) / 0 (rejected), remaining count]
        // But since RedisScript requires a single return type, we can return the remaining count. 
        // If remaining count < 0, it means rejected.
        
        String script = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            
            -- Remove old requests
            redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
            
            -- Count current requests
            local count = redis.call('ZCARD', key)
            
            if count < limit then
                redis.call('ZADD', key, now, now .. '-' .. math.random())
                redis.call('PEXPIRE', key, window)
                return limit - count - 1
            else
                return -1
            end
            """;
        
        this.slidingWindowScript = new DefaultRedisScript<>(script, Long.class);
    }

    /**
     * Checks if the request is allowed.
     * @param identifier Client identifier (e.g., IP or user ID)
     * @param limit Max requests per window
     * @param windowSeconds Window size in seconds
     * @return Remaining requests, or -1 if rate limited
     */
    public long checkAndConsume(String identifier, int limit, int windowSeconds) {
        String key = "rate_limit:" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowMillis = windowSeconds * 1000L;
        
        Long remaining = redisTemplate.execute(
            slidingWindowScript,
            Collections.singletonList(key),
            String.valueOf(now),
            String.valueOf(windowMillis),
            String.valueOf(limit)
        );
        
        return remaining != null ? remaining : -1;
    }
}
