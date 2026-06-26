package az.unibank.smartorder.order.adapter.inbound.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    BigDecimal price,
    String currency,
    int stockQuantity
) {}
