package az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderItemJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderJpaEntity toJpaEntity(Order order);

    @AfterMapping
    default void linkOrderItems(@MappingTarget OrderJpaEntity orderJpaEntity) {
        if (orderJpaEntity.getItems() != null) {
            orderJpaEntity.getItems().forEach(item -> item.setOrder(orderJpaEntity));
        }
    }

    @Mapping(target = "id", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.OrderId.of(entity.getId()))")
    @Mapping(target = "customerId", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.CustomerId.of(entity.getCustomerId()))")
    @Mapping(target = "totalAmount", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getTotalAmount(), entity.getCurrency()))")
    Order toDomainModel(OrderJpaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "productNameSnapshot", source = "productName")
    @Mapping(target = "unitPrice", source = "unitPrice.amount")
    @Mapping(target = "currency", source = "unitPrice.currency")
    @Mapping(target = "order", ignore = true)
    OrderItemJpaEntity toJpaEntity(OrderItem item);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.ProductId.of(entity.getProductId()))")
    @Mapping(target = "productName", source = "productNameSnapshot")
    @Mapping(target = "unitPrice", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getUnitPrice(), entity.getCurrency()))")
    OrderItem toDomainModel(OrderItemJpaEntity entity);
}
