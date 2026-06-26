package az.unibank.smartorder.payment.domain.model.entity;

import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentTransactionId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentTransaction {
    private final PaymentTransactionId id;
    private final PaymentId paymentId;
    private final Integer attemptNumber;
    private final String gatewayReference;
    private final PaymentStatus status;
    private final String errorMessage;
    private final Instant executedAt;

    public static PaymentTransaction create(PaymentId paymentId, Integer attemptNumber, String gatewayReference, PaymentStatus status, String errorMessage) {
        return PaymentTransaction.builder()
                .id(PaymentTransactionId.of(java.util.UUID.randomUUID()))
                .paymentId(paymentId)
                .attemptNumber(attemptNumber)
                .gatewayReference(gatewayReference)
                .status(status)
                .errorMessage(errorMessage)
                .executedAt(Instant.now())
                .build();
    }
}
