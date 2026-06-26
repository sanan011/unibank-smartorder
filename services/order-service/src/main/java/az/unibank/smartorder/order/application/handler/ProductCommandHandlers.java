package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.order.application.command.UpdateStockCommand;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.domain.port.inbound.UpdateStockUseCase;
import az.unibank.smartorder.order.domain.port.outbound.ProductCacheRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class ProductCommandHandlers implements UpdateStockUseCase {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    @Override
    @Transactional
    public void updateStock(UpdateStockCommand command) {
        Product product = productRepository.findByIdWithPessimisticLock(ProductId.of(command.getProductId()))
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));
        
        product.updateStock(StockQuantity.of(command.getQuantity()));
        Product saved = productRepository.save(product);
        productCacheRepository.put(saved);
    }
}

