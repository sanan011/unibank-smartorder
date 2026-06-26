package az.unibank.smartorder.order.infrastructure.persistence.jpa.repository;

import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query(value = "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 50 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEventJpaEntity> findPendingEventsForProcessing();
}
