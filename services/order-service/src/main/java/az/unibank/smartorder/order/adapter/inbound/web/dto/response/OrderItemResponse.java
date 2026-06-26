package az.unibank.smartorder.order.adapter.inbound.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID productId,
    String productName,
    BigDecimal unitPrice,
    String currency,
    int quantity,
    BigDecimal subTotal
) {}
