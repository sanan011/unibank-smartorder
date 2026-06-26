package az.unibank.smartorder.identity.infrastructure.cache;

import az.unibank.smartorder.security.TokenBlocklist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring singleton injection")
public class RedisTokenBlocklist implements TokenBlocklist {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isBlocked(String subject) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blocklist:user:" + subject));
    }
}
