package az.unibank.smartorder.identity.infrastructure.cache;

import az.unibank.smartorder.identity.domain.model.valueobject.UserId;
import az.unibank.smartorder.identity.domain.port.outbound.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring singleton injection")
public class RedisRefreshTokenAdapter implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UserId userId, String tokenId, long ttlSeconds) {
        String key = buildKey(userId, tokenId);
        // We just need to store presence, so value can be a simple flag
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean exists(UserId userId, String tokenId) {
        String key = buildKey(userId, tokenId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void revoke(UserId userId, String tokenId) {
        String key = buildKey(userId, tokenId);
        redisTemplate.delete(key);
    }

    private String buildKey(UserId userId, String tokenId) {
        return KEY_PREFIX + userId.value() + ":" + tokenId;
    }

    @Override
    public void blockUserTokens(UserId userId) {
        redisTemplate.opsForValue().set("blocklist:user:" + userId.value(), "1", Duration.ofHours(1));
    }

    @Override
    public boolean isUserTokensBlocked(UserId userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blocklist:user:" + userId.value()));
    }
}
