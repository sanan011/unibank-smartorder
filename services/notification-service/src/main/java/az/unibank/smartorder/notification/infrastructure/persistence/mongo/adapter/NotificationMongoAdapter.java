package az.unibank.smartorder.notification.infrastructure.persistence.mongo.adapter;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationId;
import az.unibank.smartorder.notification.domain.port.outbound.NotificationRepository;
import az.unibank.smartorder.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import az.unibank.smartorder.notification.infrastructure.persistence.mongo.mapper.NotificationMapper;
import az.unibank.smartorder.notification.infrastructure.persistence.mongo.repository.SpringDataMongoNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationMongoAdapter implements NotificationRepository {

    private final SpringDataMongoNotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationDocument document = mapper.domainToEntity(notification);
        NotificationDocument saved = repository.save(document);
        return mapper.entityToDomain(saved);
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return repository.findById(id.getValue()).map(mapper::entityToDomain);
    }

    @Override
    public Optional<Notification> findFirstByOrderId(UUID orderId) {
        return repository.findFirstByOrderIdOrderByCreatedAtDesc(orderId).map(mapper::entityToDomain);
    }

    @Override
    public List<Notification> findByCustomerId(UUID customerId, int page, int size) {
        return repository.findByCustomerId(customerId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(mapper::entityToDomain)
                .collect(Collectors.toList());
    }
}
