package az.unibank.smartorder.order.adapter.inbound.web.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateStockRequest(
    @Min(value = 0, message = "Stock quantity cannot be negative")
    int quantity
) {}
