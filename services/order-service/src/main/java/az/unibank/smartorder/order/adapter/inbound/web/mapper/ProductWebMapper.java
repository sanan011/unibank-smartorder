package az.unibank.smartorder.order.adapter.inbound.web.mapper;

import az.unibank.smartorder.order.adapter.inbound.web.dto.response.ProductResponse;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductWebMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "currency", source = "price.currency")
    @Mapping(target = "stockQuantity", source = "stockQuantity.value")
    ProductResponse toResponse(Product product);
}
