package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.order.application.query.ListProductsQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.port.inbound.GetProductUseCase;
import az.unibank.smartorder.order.domain.port.inbound.ListProductsUseCase;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class ProductQueryHandlers implements GetProductUseCase, ListProductsUseCase {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    @Override
    public Product getProduct(UUID productId) {
        ProductId id = ProductId.of(productId);
        
        // 1. Fast path: return from cache if present
        var cached = productCacheRepository.get(id);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Cache miss: attempt to acquire distributed lock
        if (productCacheRepository.tryLock(id)) {
            try {
                // Double-checked locking
                cached = productCacheRepository.get(id);
                if (cached.isPresent()) {
                    return cached.get();
                }

                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));
                productCacheRepository.put(product);
                return product;
            } finally {
                productCacheRepository.releaseLock(id);
            }
        } else {
            // 3. Lock not acquired: another thread is fetching data. Wait and retry cache.
            int maxRetries = 10;
            int waitMs = 50;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("INTERNAL_ERROR", "Interrupted while waiting for cache", 500);
                }
                
                cached = productCacheRepository.get(id);
                if (cached.isPresent()) {
                    return cached.get();
                }
            }
            
            // 4. Fallback: if cache is still not populated after waiting, query DB directly to serve request.
            return productRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));
        }
    }

    @Override
    public Page<Product> listProducts(ListProductsQuery query) {
        return productRepository.findAll(PageRequest.of(query.getPage(), query.getSize()));
    }
}

