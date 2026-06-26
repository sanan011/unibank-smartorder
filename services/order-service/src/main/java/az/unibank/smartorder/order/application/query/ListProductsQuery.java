package az.unibank.smartorder.order.application.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListProductsQuery {
    private final int page;
    private final int size;
}
