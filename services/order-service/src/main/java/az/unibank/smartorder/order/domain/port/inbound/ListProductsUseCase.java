package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.query.ListProductsQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import org.springframework.data.domain.Page;

public interface ListProductsUseCase {
    Page<Product> listProducts(ListProductsQuery query);
}
