package az.unibank.smartorder.order.adapter.inbound.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public record OrderResponse(
    UUID id,
    UUID customerId,
    String status,
    List<OrderItemResponse> items,
    BigDecimal totalAmount,
    String currency,
    Instant createdAt,
    Instant updatedAt
) {}

