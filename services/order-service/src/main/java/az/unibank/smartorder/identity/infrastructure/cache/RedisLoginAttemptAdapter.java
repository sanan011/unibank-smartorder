package az.unibank.smartorder.identity.infrastructure.cache;

import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.port.outbound.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring singleton injection")
public class RedisLoginAttemptAdapter implements LoginAttemptRepository {

    private static final String ATTEMPTS_PREFIX = "login_attempts:";
    private static final String LOCKOUT_PREFIX = "login_lockout:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void recordFailedAttempt(Email email) {
        String attemptsKey = ATTEMPTS_PREFIX + email.value();
        String lockoutKey = LOCKOUT_PREFIX + email.value();

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockoutKey, "LOCKED", Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
            redisTemplate.delete(attemptsKey);
        }
    }

    @Override
    public void resetAttempts(Email email) {
        redisTemplate.delete(ATTEMPTS_PREFIX + email.value());
        redisTemplate.delete(LOCKOUT_PREFIX + email.value());
    }

    @Override
    public boolean isLocked(Email email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCKOUT_PREFIX + email.value()));
    }
}
