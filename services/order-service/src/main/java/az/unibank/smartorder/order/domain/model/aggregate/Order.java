package az.unibank.smartorder.order.domain.model.aggregate;

import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderStatus;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private Money totalAmount;
    private Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    public void validate() {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("ORDER_EMPTY", "Order must have at least one item", 400);
        }
        
        Money calculatedTotal = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(Money.ZERO, Money::add);
                
        if (!calculatedTotal.equals(totalAmount)) {
            throw new BusinessException("INVALID_TOTAL", "Order total amount does not match items subtotal", 400);
        }
    }

    public void initialize() {
        this.status = OrderStatus.PENDING;
        validate();
    }

    public void processPayment() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("INVALID_TRANSITION", "Order must be PENDING to process payment", 400);
        }
        this.status = OrderStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void pay() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new BusinessException("INVALID_TRANSITION", "Order must be PROCESSING to be PAID", 400);
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void failPayment() {
        if (this.status == OrderStatus.PAYMENT_FAILED) {
            return; // Idempotent: already failed
        }
        if (this.status != OrderStatus.PROCESSING) {
            throw new BusinessException("INVALID_TRANSITION", "Order must be PROCESSING to fail payment", 400);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("INVALID_TRANSITION", "Only PENDING orders can be cancelled", 400);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
}

