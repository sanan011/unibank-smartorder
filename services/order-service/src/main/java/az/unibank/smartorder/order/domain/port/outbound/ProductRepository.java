package az.unibank.smartorder.order.domain.port.outbound;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    Optional<Product> findByIdWithPessimisticLock(ProductId id);
    Product save(Product product);
    Page<Product> findAll(Pageable pageable);
}
