package az.unibank.smartorder.order.infrastructure.cache;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class CacheWarmupService {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Starting cache warm-up for products...");
        int page = 0;
        int size = 100;
        Page<Product> productPage;
        int count = 0;
        
        do {
            productPage = productRepository.findAll(PageRequest.of(page, size));
            for (Product product : productPage.getContent()) {
                productCacheRepository.put(product);
                count++;
            }
            page++;
        } while (productPage.hasNext());
        
        log.info("Cache warm-up completed. Loaded {} products into Redis.", count);
    }
}

