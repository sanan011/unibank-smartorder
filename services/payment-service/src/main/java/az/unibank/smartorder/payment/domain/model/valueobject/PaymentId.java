package az.unibank.smartorder.payment.domain.model.valueobject;

import java.util.UUID;

public record PaymentId(UUID value) {
    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}
