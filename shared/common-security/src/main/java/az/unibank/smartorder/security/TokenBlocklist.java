package az.unibank.smartorder.security;

public interface TokenBlocklist {
    boolean isBlocked(String subject);
    void blockToken(String jti, long ttlMs);
    boolean isTokenBlocked(String jti);
}
