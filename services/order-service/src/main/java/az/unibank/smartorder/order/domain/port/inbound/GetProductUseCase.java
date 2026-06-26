package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import java.util.UUID;

public interface GetProductUseCase {
    Product getProduct(UUID productId);
}
