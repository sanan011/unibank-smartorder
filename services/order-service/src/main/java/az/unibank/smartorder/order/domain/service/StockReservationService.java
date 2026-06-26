package az.unibank.smartorder.order.domain.service;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockReservationService {

    private final ProductRepository productRepository;

    public void reserveStockFor(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));

            if (product.getStockQuantity().getValue() < item.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", "Not enough stock for product " + product.getName(), 400);
            }

            product.decreaseStock(item.getQuantity());
            productRepository.save(product);
        }
    }
}
