package az.unibank.smartorder.order.domain.service;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class StockReservationService {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    public void reserveStockFor(Order order) {
        List<OrderItem> sortedItems = order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProductId().getValue()))
                .collect(Collectors.toList());

        for (OrderItem item : sortedItems) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));

            if (product.getStockQuantity().getValue() < item.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", "Not enough stock for product " + product.getName(), 400);
            }

            product.decreaseStock(item.getQuantity());
            Product saved = productRepository.save(product);
            productCacheRepository.put(saved);
        }
    }

    public void releaseStockFor(Order order) {
        List<OrderItem> sortedItems = order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProductId().getValue()))
                .collect(Collectors.toList());

        for (OrderItem item : sortedItems) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));

            product.increaseStock(item.getQuantity());
            Product saved = productRepository.save(product);
            productCacheRepository.put(saved);
        }
    }
}
