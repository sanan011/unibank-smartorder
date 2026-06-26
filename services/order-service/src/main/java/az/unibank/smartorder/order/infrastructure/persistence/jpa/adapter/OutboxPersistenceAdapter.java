package az.unibank.smartorder.order.infrastructure.persistence.jpa.adapter;

import az.unibank.smartorder.events.DomainEvent;
import az.unibank.smartorder.order.domain.port.outbound.OutboxRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OutboxEventJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.OutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class OutboxPersistenceAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(DomainEvent event) {
        try {
            OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                    .id(event.eventId())
                    .aggregateType("Order")
                    .aggregateId(event.correlationId())
                    .eventType(event.eventType())
                    .payload(objectMapper.writeValueAsString(event))
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();
            outboxJpaRepository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event", e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}

