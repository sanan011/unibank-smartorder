package az.unibank.smartorder.identity.domain.model.aggregate;

import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.model.valueobject.PasswordHash;
import az.unibank.smartorder.identity.domain.model.valueobject.Role;
import az.unibank.smartorder.identity.domain.model.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class User {
    private final UserId id;
    private final Email email;
    private PasswordHash passwordHash;
    private Role role;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;

    public static User create(Email email, PasswordHash passwordHash, Role role) {
        Instant now = Instant.now();
        return User.builder()
                .id(UserId.generate())
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updatePassword(PasswordHash newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }
}
