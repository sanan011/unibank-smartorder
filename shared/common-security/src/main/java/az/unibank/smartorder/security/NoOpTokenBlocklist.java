package az.unibank.smartorder.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(TokenBlocklist.class)
public class NoOpTokenBlocklist implements TokenBlocklist {
    @Override
    public boolean isBlocked(String subject) {
        return false;
    }

    @Override
    public void blockToken(String jti, long ttlMs) {
        // No-op
    }

    @Override
    public boolean isTokenBlocked(String jti) {
        return false;
    }
}
