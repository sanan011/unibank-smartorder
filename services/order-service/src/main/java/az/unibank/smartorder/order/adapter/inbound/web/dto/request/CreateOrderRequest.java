package az.unibank.smartorder.order.adapter.inbound.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public record CreateOrderRequest(
    @NotEmpty(message = "Order must have at least one item")
    @Size(max = 50, message = "Cannot order more than 50 distinct products")
    List<@Valid OrderItemRequest> items
) {}

