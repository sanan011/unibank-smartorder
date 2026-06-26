package az.unibank.smartorder.order.infrastructure.persistence.jpa.repository;

import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Page<OrderJpaEntity> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
    Page<OrderJpaEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
