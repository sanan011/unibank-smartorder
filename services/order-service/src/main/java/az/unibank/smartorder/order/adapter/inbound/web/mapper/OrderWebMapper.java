package az.unibank.smartorder.order.adapter.inbound.web.mapper;

import az.unibank.smartorder.order.adapter.inbound.web.dto.response.OrderItemResponse;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.OrderResponse;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderWebMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency")
    OrderResponse toResponse(Order order);

    List<OrderItemResponse> toItemResponses(List<OrderItem> items);

    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "unitPrice", source = "unitPrice.amount")
    @Mapping(target = "currency", source = "unitPrice.currency")
    @Mapping(target = "subTotal", expression = "java(item.getSubTotal().getAmount())")
    OrderItemResponse toItemResponse(OrderItem item);
}
