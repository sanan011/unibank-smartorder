package az.unibank.smartorder.order.infrastructure.persistence.jpa.repository;

import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    // Eagerly fetch items so the persistence mapper can read them after the session closes
    // (the @OneToMany is LAZY; mapping a detached order otherwise throws LazyInitializationException).
    @EntityGraph(attributePaths = "items")
    Optional<OrderJpaEntity> findById(UUID id);

    Page<OrderJpaEntity> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
    Page<OrderJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
