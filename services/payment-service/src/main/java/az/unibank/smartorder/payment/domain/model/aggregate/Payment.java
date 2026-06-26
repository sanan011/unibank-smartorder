package az.unibank.smartorder.payment.domain.model.aggregate;

import az.unibank.smartorder.payment.domain.model.entity.PaymentTransaction;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class Payment {
    private final PaymentId id;
    private final UUID orderId;
    private final UUID customerId;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private Integer attemptCount;
    private Instant lastAttemptAt;
    private Long version;
    private final Instant createdAt;
    private Instant updatedAt;
    
    @Builder.Default
    private final List<PaymentTransaction> transactions = new ArrayList<>();

    public void process() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment must be in PENDING state to be processed");
        }
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void resetToPending() {
        if (this.status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Payment must be in FAILED state to be reset to PENDING");
        }
        this.status = PaymentStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    public void markSuccess(String gatewayReference) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment must be in PROCESSING state to be marked as success");
        }
        this.status = PaymentStatus.SUCCESS;
        this.attemptCount++;
        this.lastAttemptAt = Instant.now();
        this.updatedAt = Instant.now();
        
        this.transactions.add(PaymentTransaction.create(
                this.id,
                this.attemptCount,
                gatewayReference,
                PaymentStatus.SUCCESS,
                null
        ));
    }

    public void markFailed(String errorMessage) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment must be in PROCESSING state to be marked as failed");
        }
        this.status = PaymentStatus.FAILED;
        this.attemptCount++;
        this.lastAttemptAt = Instant.now();
        this.updatedAt = Instant.now();
        
        this.transactions.add(PaymentTransaction.create(
                this.id,
                this.attemptCount,
                null,
                PaymentStatus.FAILED,
                errorMessage
        ));
    }

    public List<PaymentTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
