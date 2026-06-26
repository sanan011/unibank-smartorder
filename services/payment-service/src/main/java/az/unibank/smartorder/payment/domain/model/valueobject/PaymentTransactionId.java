package az.unibank.smartorder.payment.domain.model.valueobject;

import java.util.UUID;

public record PaymentTransactionId(UUID value) {
    public static PaymentTransactionId of(UUID value) {
        return new PaymentTransactionId(value);
    }
}
