package az.unibank.smartorder.security;

public interface TokenBlocklist {
    boolean isBlocked(String subject);
}
