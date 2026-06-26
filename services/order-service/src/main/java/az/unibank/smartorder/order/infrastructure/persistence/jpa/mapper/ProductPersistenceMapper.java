package az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "currency", source = "price.currency")
    @Mapping(target = "stockQuantity", source = "stockQuantity.value")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductJpaEntity toJpaEntity(Product product);

    @Mapping(target = "id", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.ProductId.of(entity.getId()))")
    @Mapping(target = "price", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getPrice(), entity.getCurrency()))")
    @Mapping(target = "stockQuantity", expression = "java(az.unibank.smartorder.order.domain.model.valueobject.StockQuantity.of(entity.getStockQuantity()))")
    Product toDomainModel(ProductJpaEntity entity);
}
