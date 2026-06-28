package az.unibank.smartorder.notification.infrastructure.persistence.mongo.mapper;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", source = "id.value")
    NotificationDocument domainToEntity(Notification notification);

    @Mapping(target = "id", expression = "java(az.unibank.smartorder.notification.domain.model.valueobject.NotificationId.of(entity.getId()))")
    Notification entityToDomain(NotificationDocument entity);
}
