package az.unibank.smartorder.order.domain.port.outbound;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import java.util.Optional;

public interface ProductCacheRepository {
    Optional<Product> get(ProductId id);
    void put(Product product);
    void invalidate(ProductId id);
    boolean tryLock(ProductId id);
    void releaseLock(ProductId id);
}
