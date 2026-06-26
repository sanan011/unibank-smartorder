package az.unibank.smartorder.order.infrastructure.cache;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class RedisProductCacheAdapter implements ProductCacheRepository {

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String LOCK_KEY_PREFIX = "lock:product:";
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedProduct {
        private UUID id;
        private String name;
        private BigDecimal price;
        private String currency;
        private int stockQuantity;
        private Long version;
    }

    @Override
    public Optional<Product> get(ProductId id) {
        String key = PRODUCT_KEY_PREFIX + id.getValue();
        String json = redisTemplate.opsForValue().get(key);
        
        if (json != null) {
            try {
                CachedProduct cached = objectMapper.readValue(json, CachedProduct.class);
                Product product = Product.builder()
                        .id(ProductId.of(cached.getId()))
                        .name(cached.getName())
                        .price(Money.of(cached.getPrice(), cached.getCurrency()))
                        .stockQuantity(StockQuantity.of(cached.getStockQuantity()))
                        .version(cached.getVersion())
                        .build();
                return Optional.of(product);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize product from cache: {}", key, e);
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(Product product) {
        String key = PRODUCT_KEY_PREFIX + product.getId().getValue();
        try {
            CachedProduct cached = CachedProduct.builder()
                    .id(product.getId().getValue())
                    .name(product.getName())
                    .price(product.getPrice().getAmount())
                    .currency(product.getPrice().getCurrency())
                    .stockQuantity(product.getStockQuantity().getValue())
                    .version(product.getVersion())
                    .build();
            String json = objectMapper.writeValueAsString(cached);
            redisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize product for cache: {}", key, e);
        }
    }

    @Override
    public void invalidate(ProductId id) {
        String key = PRODUCT_KEY_PREFIX + id.getValue();
        redisTemplate.delete(key);
    }

    @Override
    public boolean tryLock(ProductId id) {
        String lockKey = LOCK_KEY_PREFIX + id.getValue();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 5, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void releaseLock(ProductId id) {
        String lockKey = LOCK_KEY_PREFIX + id.getValue();
        redisTemplate.delete(lockKey);
    }
}

