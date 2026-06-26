package az.unibank.smartorder.payment.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.entity.PaymentTransaction;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentTransactionId;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentTransactionJpaEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T18:29:57+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.7.jar, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class PaymentPersistenceMapperImpl implements PaymentPersistenceMapper {

    @Override
    public PaymentJpaEntity toJpaEntity(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentJpaEntity.PaymentJpaEntityBuilder paymentJpaEntity = PaymentJpaEntity.builder();

        paymentJpaEntity.id( paymentIdValue( payment ) );
        paymentJpaEntity.orderId( payment.getOrderId() );
        paymentJpaEntity.customerId( payment.getCustomerId() );
        paymentJpaEntity.amount( payment.getAmount() );
        paymentJpaEntity.currency( payment.getCurrency() );
        paymentJpaEntity.status( payment.getStatus() );
        paymentJpaEntity.attemptCount( payment.getAttemptCount() );
        paymentJpaEntity.lastAttemptAt( payment.getLastAttemptAt() );
        paymentJpaEntity.version( payment.getVersion() );
        paymentJpaEntity.createdAt( payment.getCreatedAt() );
        paymentJpaEntity.updatedAt( payment.getUpdatedAt() );
        paymentJpaEntity.transactions( paymentTransactionListToPaymentTransactionJpaEntityList( payment.getTransactions() ) );

        return paymentJpaEntity.build();
    }

    @Override
    public Payment toDomainModel(PaymentJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Payment.PaymentBuilder payment = Payment.builder();

        payment.orderId( entity.getOrderId() );
        payment.customerId( entity.getCustomerId() );
        payment.amount( entity.getAmount() );
        payment.currency( entity.getCurrency() );
        payment.status( entity.getStatus() );
        payment.attemptCount( entity.getAttemptCount() );
        payment.lastAttemptAt( entity.getLastAttemptAt() );
        payment.version( entity.getVersion() );
        payment.createdAt( entity.getCreatedAt() );
        payment.updatedAt( entity.getUpdatedAt() );
        payment.transactions( paymentTransactionJpaEntityListToPaymentTransactionList( entity.getTransactions() ) );

        payment.id( az.unibank.smartorder.payment.domain.model.valueobject.PaymentId.of(entity.getId()) );

        return payment.build();
    }

    @Override
    public PaymentTransactionJpaEntity toJpaEntity(PaymentTransaction tx) {
        if ( tx == null ) {
            return null;
        }

        PaymentTransactionJpaEntity.PaymentTransactionJpaEntityBuilder paymentTransactionJpaEntity = PaymentTransactionJpaEntity.builder();

        paymentTransactionJpaEntity.id( txIdValue( tx ) );
        paymentTransactionJpaEntity.attemptNumber( tx.getAttemptNumber() );
        paymentTransactionJpaEntity.gatewayReference( tx.getGatewayReference() );
        paymentTransactionJpaEntity.status( tx.getStatus() );
        paymentTransactionJpaEntity.errorMessage( tx.getErrorMessage() );
        paymentTransactionJpaEntity.executedAt( tx.getExecutedAt() );

        return paymentTransactionJpaEntity.build();
    }

    @Override
    public PaymentTransaction toDomainModel(PaymentTransactionJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PaymentTransaction.PaymentTransactionBuilder paymentTransaction = PaymentTransaction.builder();

        paymentTransaction.attemptNumber( entity.getAttemptNumber() );
        paymentTransaction.gatewayReference( entity.getGatewayReference() );
        paymentTransaction.status( entity.getStatus() );
        paymentTransaction.errorMessage( entity.getErrorMessage() );
        paymentTransaction.executedAt( entity.getExecutedAt() );

        paymentTransaction.id( az.unibank.smartorder.payment.domain.model.valueobject.PaymentTransactionId.of(entity.getId()) );
        paymentTransaction.paymentId( az.unibank.smartorder.payment.domain.model.valueobject.PaymentId.of(entity.getPayment().getId()) );

        return paymentTransaction.build();
    }

    private UUID paymentIdValue(Payment payment) {
        if ( payment == null ) {
            return null;
        }
        PaymentId id = payment.getId();
        if ( id == null ) {
            return null;
        }
        UUID value = id.value();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    protected List<PaymentTransactionJpaEntity> paymentTransactionListToPaymentTransactionJpaEntityList(List<PaymentTransaction> list) {
        if ( list == null ) {
            return null;
        }

        List<PaymentTransactionJpaEntity> list1 = new ArrayList<PaymentTransactionJpaEntity>( list.size() );
        for ( PaymentTransaction paymentTransaction : list ) {
            list1.add( toJpaEntity( paymentTransaction ) );
        }

        return list1;
    }

    protected List<PaymentTransaction> paymentTransactionJpaEntityListToPaymentTransactionList(List<PaymentTransactionJpaEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<PaymentTransaction> list1 = new ArrayList<PaymentTransaction>( list.size() );
        for ( PaymentTransactionJpaEntity paymentTransactionJpaEntity : list ) {
            list1.add( toDomainModel( paymentTransactionJpaEntity ) );
        }

        return list1;
    }

    private UUID txIdValue(PaymentTransaction paymentTransaction) {
        if ( paymentTransaction == null ) {
            return null;
        }
        PaymentTransactionId id = paymentTransaction.getId();
        if ( id == null ) {
            return null;
        }
        UUID value = id.value();
        if ( value == null ) {
            return null;
        }
        return value;
    }
}
