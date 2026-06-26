package az.unibank.smartorder.order.adapter.inbound.web.controller;

import az.unibank.smartorder.order.adapter.inbound.web.dto.request.CreateOrderRequest;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.OrderResponse;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.PagedResponse;
import az.unibank.smartorder.order.adapter.inbound.web.mapper.OrderWebMapper;
import az.unibank.smartorder.order.application.command.CancelOrderCommand;
import az.unibank.smartorder.order.application.command.CreateOrderCommand;
import az.unibank.smartorder.order.application.query.GetOrderQuery;
import az.unibank.smartorder.order.application.query.ListOrdersQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.port.inbound.CancelOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.CreateOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.GetOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.ListOrdersUseCase;
import az.unibank.smartorder.security.UserPrincipal;
import az.unibank.smartorder.web.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final OrderWebMapper orderWebMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(userPrincipal.getId())
                .items(request.items().stream()
                        .map(item -> CreateOrderCommand.OrderItemCommand.builder()
                                .productId(item.productId())
                                .quantity(item.quantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
                
        Order order = createOrderUseCase.createOrder(command);
        return orderWebMapper.toResponse(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public PagedResponse<OrderResponse> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
            
        ListOrdersQuery query = new ListOrdersQuery(userPrincipal.getId(), status, page, size);
        Page<Order> orderPage = listOrdersUseCase.listOrders(query);
        
        return new PagedResponse<>(
                orderPage.getContent().stream().map(orderWebMapper::toResponse).toList(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public OrderResponse getOrder(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Order order = getOrderUseCase.getOrder(new GetOrderQuery(id));
        if (!order.getCustomerId().getValue().equals(userPrincipal.getId()) && !userPrincipal.getRole().equals("ADMIN")) {
            throw new BusinessException("ACCESS_DENIED", "You do not have permission to view this order", 403);
        }
        return orderWebMapper.toResponse(order);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public OrderResponse cancelOrder(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Order order = getOrderUseCase.getOrder(new GetOrderQuery(id));
        if (!order.getCustomerId().getValue().equals(userPrincipal.getId()) && !userPrincipal.getRole().equals("ADMIN")) {
            throw new BusinessException("ACCESS_DENIED", "You do not have permission to cancel this order", 403);
        }
        
        Order cancelledOrder = cancelOrderUseCase.cancelOrder(new CancelOrderCommand(id));
        return orderWebMapper.toResponse(cancelledOrder);
    }
}
