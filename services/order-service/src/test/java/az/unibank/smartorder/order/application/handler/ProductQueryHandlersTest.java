package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductQueryHandlersTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheRepository productCacheRepository;

    private ProductQueryHandlers productQueryHandlers;

    private Product sampleProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productQueryHandlers = new ProductQueryHandlers(productRepository, productCacheRepository);
        productId = UUID.randomUUID();
        sampleProduct = Product.builder()
                .id(ProductId.of(productId))
                .name("Test Product")
                .price(Money.of(new BigDecimal("10.00"), "USD"))
                .stockQuantity(StockQuantity.of(100))
                .version(1L)
                .build();
    }

    @Test
    void getProduct_whenCacheHit_returnsFromCache() {
        when(productCacheRepository.get(any(ProductId.class))).thenReturn(Optional.of(sampleProduct));

        Product result = productQueryHandlers.getProduct(productId);

        assertThat(result).isEqualTo(sampleProduct);
        verify(productCacheRepository).get(ProductId.of(productId));
        verify(productRepository, times(0)).findById(any());
        verify(productCacheRepository, times(0)).tryLock(any());
    }

    @Test
    void getProduct_concurrentCacheMiss_onlyOneDatabaseLoad() throws InterruptedException {
        // We simulate 10 concurrent requests
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger dbCallCount = new AtomicInteger(0);
        AtomicInteger cachePutCount = new AtomicInteger(0);

        // First call to cache.get() returns empty
        lenient().when(productCacheRepository.get(ProductId.of(productId)))
                .thenReturn(Optional.empty());

        // We want tryLock to return true only ONCE
        AtomicInteger lockAcquiredCount = new AtomicInteger(0);
        doAnswer(invocation -> lockAcquiredCount.getAndIncrement() == 0)
                .when(productCacheRepository).tryLock(ProductId.of(productId));

        // DB fetch will increment db counter and take a little time to simulate latency
        lenient().doAnswer(invocation -> {
            dbCallCount.incrementAndGet();
            Thread.sleep(100);
            
            // Once DB is fetched, simulate cache being populated
            lenient().when(productCacheRepository.get(ProductId.of(productId)))
                    .thenReturn(Optional.of(sampleProduct));
            
            return Optional.of(sampleProduct);
        }).when(productRepository).findById(ProductId.of(productId));

        // Track cache puts
        lenient().doAnswer(invocation -> {
            cachePutCount.incrementAndGet();
            return null;
        }).when(productCacheRepository).put(any(Product.class));

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    productQueryHandlers.getProduct(productId);
                } catch (Exception e) {
                    // Ignore
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Go!
        boolean completed = endLatch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        assertThat(dbCallCount.get()).isEqualTo(1); // exactly 1 DB call
        assertThat(cachePutCount.get()).isEqualTo(1); // exactly 1 cache put
        verify(productCacheRepository, times(1)).releaseLock(ProductId.of(productId)); // lock released once
    }

    @Test
    void getProduct_whenLockTimesOut_fallsBackToDb() {
        // Cache misses
        when(productCacheRepository.get(any(ProductId.class))).thenReturn(Optional.empty());
        // Lock never acquired
        when(productCacheRepository.tryLock(any(ProductId.class))).thenReturn(false);
        // DB finds the product
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(sampleProduct));

        Product result = productQueryHandlers.getProduct(productId);

        assertThat(result).isEqualTo(sampleProduct);
        // DB should be called because cache wait timed out
        verify(productRepository, times(1)).findById(ProductId.of(productId));
    }
}
