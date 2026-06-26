package az.unibank.smartorder.identity.domain.port.outbound;

import az.unibank.smartorder.identity.domain.model.valueobject.UserId;

public interface RefreshTokenRepository {
    void save(UserId userId, String tokenId, long ttlSeconds);
    boolean exists(UserId userId, String tokenId);
    void revoke(UserId userId, String tokenId);
    void blockUserTokens(UserId userId);
    boolean isUserTokensBlocked(UserId userId);
}
