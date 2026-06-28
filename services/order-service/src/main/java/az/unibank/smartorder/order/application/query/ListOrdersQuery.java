package az.unibank.smartorder.order.application.query;

import lombok.Getter;

import java.util.UUID;

import az.unibank.smartorder.web.exception.BusinessException;

@Getter
public final class ListOrdersQuery {
    private final UUID customerId;
    private final String status;
    private final int page;
    private final int size;

    public ListOrdersQuery(UUID customerId, String status, int page, int size) {
        if (size > 100) {
            throw new BusinessException("BAD_REQUEST", "Page size cannot exceed 100", 400);
        }
        this.customerId = customerId;
        this.status = status;
        this.page = page;
        this.size = size;
    }
}
