package az.unibank.smartorder.identity.domain.port.outbound;

import az.unibank.smartorder.identity.domain.model.aggregate.User;
import az.unibank.smartorder.identity.domain.model.valueobject.Email;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(Email email);
    Optional<User> findById(az.unibank.smartorder.identity.domain.model.valueobject.UserId id);
    boolean existsByEmail(Email email);
}
