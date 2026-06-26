package az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity;

import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "JPA Entity requirement")
public class PaymentTransactionJpaEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentJpaEntity payment;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;
}
