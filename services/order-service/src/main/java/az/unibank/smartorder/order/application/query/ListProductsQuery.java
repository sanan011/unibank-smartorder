package az.unibank.smartorder.order.application.query;

import lombok.Getter;

import az.unibank.smartorder.web.exception.BusinessException;
import java.util.Set;

@Getter
public final class ListProductsQuery {
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price", "stockQuantity");

    public ListProductsQuery(int page, int size) {
        this(page, size, "name", "ASC");
    }

    public ListProductsQuery(int page, int size, String sortBy, String sortDirection) {
        if (size > 100) {
            throw new BusinessException("BAD_REQUEST", "Page size cannot exceed 100", 400);
        }
        
        String finalSortBy = sortBy != null ? sortBy : "name";
        if (!ALLOWED_SORT_FIELDS.contains(finalSortBy)) {
            finalSortBy = "name"; // fallback or could throw
        }

        this.page = page;
        this.size = size;
        this.sortBy = finalSortBy;
        this.sortDirection = sortDirection != null ? sortDirection : "ASC";
    }
}
