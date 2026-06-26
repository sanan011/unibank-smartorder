package az.unibank.smartorder.notification.infrastructure.persistence.mongo.repository;

import az.unibank.smartorder.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataMongoNotificationRepository extends MongoRepository<NotificationDocument, UUID> {
    Page<NotificationDocument> findByCustomerId(UUID customerId, Pageable pageable);
    Optional<NotificationDocument> findFirstByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
