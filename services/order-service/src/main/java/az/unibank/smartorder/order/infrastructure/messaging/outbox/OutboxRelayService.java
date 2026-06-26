package az.unibank.smartorder.order.infrastructure.messaging.outbox;

import az.unibank.smartorder.order.domain.port.outbound.EventPublisher;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OutboxEventJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {

    private final OutboxJpaRepository outboxJpaRepository;
    private final EventPublisher eventPublisher;

    // Exchange details could be configured via application.yml
    private static final String EXCHANGE = "smartorder.events";
    
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayEvents() {
        List<OutboxEventJpaEntity> pendingEvents = outboxJpaRepository.findPendingEventsForProcessing();
        
        if (pendingEvents.isEmpty()) {
            return;
        }
        
        log.info("Relaying {} pending outbox events", pendingEvents.size());
        
        for (OutboxEventJpaEntity event : pendingEvents) {
            try {
                // Determine routing key based on event type. e.g. "order.created"
                String routingKey = getRoutingKey(event.getEventType());
                
                eventPublisher.publish(EXCHANGE, routingKey, event.getPayload());
                
                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
                outboxJpaRepository.save(event);
                log.debug("Successfully relayed outbox event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to relay outbox event {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 3) {
                    event.setStatus("FAILED");
                }
                outboxJpaRepository.save(event);
            }
        }
    }

    private String getRoutingKey(String eventType) {
        if ("OrderCreatedEvent".equals(eventType)) {
            return "order.created";
        } else if ("OrderCancelledEvent".equals(eventType)) {
            return "order.cancelled";
        }
        return "order.unknown";
    }
}
