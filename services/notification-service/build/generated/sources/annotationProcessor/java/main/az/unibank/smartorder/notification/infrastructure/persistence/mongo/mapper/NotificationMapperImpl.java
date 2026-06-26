package az.unibank.smartorder.notification.infrastructure.persistence.mongo.mapper;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationId;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationStatus;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationType;
import az.unibank.smartorder.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T00:42:37+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.7.jar, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationDocument domainToEntity(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationDocument.NotificationDocumentBuilder notificationDocument = NotificationDocument.builder();

        notificationDocument.id( notificationIdValue( notification ) );
        notificationDocument.orderId( notification.getOrderId() );
        notificationDocument.customerId( notification.getCustomerId() );
        if ( notification.getType() != null ) {
            notificationDocument.type( notification.getType().name() );
        }
        if ( notification.getStatus() != null ) {
            notificationDocument.status( notification.getStatus().name() );
        }
        notificationDocument.message( notification.getMessage() );
        notificationDocument.occurredAt( notification.getOccurredAt() );
        notificationDocument.createdAt( notification.getCreatedAt() );

        return notificationDocument.build();
    }

    @Override
    public Notification entityToDomain(NotificationDocument entity) {
        if ( entity == null ) {
            return null;
        }

        Notification.NotificationBuilder notification = Notification.builder();

        notification.orderId( entity.getOrderId() );
        notification.customerId( entity.getCustomerId() );
        if ( entity.getType() != null ) {
            notification.type( Enum.valueOf( NotificationType.class, entity.getType() ) );
        }
        if ( entity.getStatus() != null ) {
            notification.status( Enum.valueOf( NotificationStatus.class, entity.getStatus() ) );
        }
        notification.message( entity.getMessage() );
        notification.occurredAt( entity.getOccurredAt() );
        notification.createdAt( entity.getCreatedAt() );

        notification.id( az.unibank.smartorder.notification.domain.model.valueobject.NotificationId.of(entity.getId()) );

        return notification.build();
    }

    private UUID notificationIdValue(Notification notification) {
        if ( notification == null ) {
            return null;
        }
        NotificationId id = notification.getId();
        if ( id == null ) {
            return null;
        }
        UUID value = id.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }
}
