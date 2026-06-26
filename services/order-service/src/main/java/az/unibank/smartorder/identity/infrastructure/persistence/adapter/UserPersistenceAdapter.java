package az.unibank.smartorder.identity.infrastructure.persistence.adapter;

import az.unibank.smartorder.identity.domain.model.aggregate.User;
import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.port.outbound.UserRepository;
import az.unibank.smartorder.identity.infrastructure.persistence.jpa.entity.UserJpaEntity;
import az.unibank.smartorder.identity.infrastructure.persistence.jpa.mapper.UserPersistenceMapper;
import az.unibank.smartorder.identity.infrastructure.persistence.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(az.unibank.smartorder.identity.domain.model.valueobject.UserId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }
}
