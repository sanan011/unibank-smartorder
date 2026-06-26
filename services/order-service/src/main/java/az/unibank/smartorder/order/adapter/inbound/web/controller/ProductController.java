package az.unibank.smartorder.order.adapter.inbound.web.controller;

import az.unibank.smartorder.order.adapter.inbound.web.dto.request.UpdateStockRequest;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.PagedResponse;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.ProductResponse;
import az.unibank.smartorder.order.adapter.inbound.web.mapper.ProductWebMapper;
import az.unibank.smartorder.order.application.command.UpdateStockCommand;
import az.unibank.smartorder.order.application.query.ListProductsQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.port.inbound.GetProductUseCase;
import az.unibank.smartorder.order.domain.port.inbound.ListProductsUseCase;
import az.unibank.smartorder.order.domain.port.inbound.UpdateStockUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateStockUseCase updateStockUseCase;
    private final ProductWebMapper productWebMapper;

    @GetMapping
    public PagedResponse<ProductResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
            
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1].toUpperCase() : "ASC";
            
        Page<Product> productPage = listProductsUseCase.listProducts(new ListProductsQuery(page, size, sortBy, sortDirection));
        
        return new PagedResponse<>(
                productPage.getContent().stream().map(productWebMapper::toResponse).toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable UUID id) {
        Product product = getProductUseCase.getProduct(id);
        return productWebMapper.toResponse(product);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStock(@PathVariable UUID id, @Valid @RequestBody UpdateStockRequest request) {
        updateStockUseCase.updateStock(new UpdateStockCommand(id, request.quantity()));
    }
}
