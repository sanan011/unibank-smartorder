package az.unibank.smartorder.identity.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.identity.domain.model.aggregate.User;
import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.model.valueobject.PasswordHash;
import az.unibank.smartorder.identity.domain.model.valueobject.Role;
import az.unibank.smartorder.identity.domain.model.valueobject.UserId;
import az.unibank.smartorder.identity.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User domain) {
        return UserJpaEntity.builder()
                .id(domain.getId().value())
                .email(domain.getEmail().value())
                .passwordHash(domain.getPasswordHash().value())
                .role(domain.getRole().name())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(new UserId(entity.getId()))
                .email(new Email(entity.getEmail()))
                .passwordHash(new PasswordHash(entity.getPasswordHash()))
                .role(Role.fromString(entity.getRole()))
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
