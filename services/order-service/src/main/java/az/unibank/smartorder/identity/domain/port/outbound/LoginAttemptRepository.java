package az.unibank.smartorder.identity.domain.port.outbound;

import az.unibank.smartorder.identity.domain.model.valueobject.Email;

public interface LoginAttemptRepository {
    void recordFailedAttempt(Email email);
    void resetAttempts(Email email);
    boolean isLocked(Email email);
}
