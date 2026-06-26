package az.unibank.smartorder.payment.infrastructure.persistence.jpa.adapter;

import az.unibank.smartorder.payment.domain.port.outbound.IdempotencyRepository;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.ProcessedEventJpaEntity;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DbIdempotencyGuardAdapter implements IdempotencyRepository {

    private final ProcessedEventJpaRepository repository;

    @Override
    public boolean exists(String eventId) {
        return repository.existsById(UUID.fromString(eventId));
    }

    @Override
    public void save(String eventId, String eventType) {
        ProcessedEventJpaEntity entity = ProcessedEventJpaEntity.builder()
                .eventId(UUID.fromString(eventId))
                .eventType(eventType)
                .processedAt(Instant.now())
                .build();
        repository.saveAndFlush(entity); // Use flush to eagerly catch constraint violations
    }
}
