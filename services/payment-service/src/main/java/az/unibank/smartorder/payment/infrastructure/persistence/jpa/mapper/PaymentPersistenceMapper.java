package az.unibank.smartorder.payment.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.entity.PaymentTransaction;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentTransactionJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    PaymentJpaEntity toJpaEntityInternal(Payment payment);

    default PaymentJpaEntity toJpaEntity(Payment payment) {
        PaymentJpaEntity entity = toJpaEntityInternal(payment);
        if (entity != null && entity.getTransactions() != null) {
            entity.getTransactions().forEach(tx -> tx.setPayment(entity));
        }
        return entity;
    }

    @BeforeMapping
    default void fixParentReferences(PaymentJpaEntity entity) {
        if (entity != null && entity.getTransactions() != null) {
            entity.getTransactions().forEach(tx -> tx.setPayment(entity));
        }
    }

    @Mapping(target = "id", expression = "java(az.unibank.smartorder.payment.domain.model.valueobject.PaymentId.of(entity.getId()))")
    Payment toDomainModel(PaymentJpaEntity entity);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "payment", ignore = true)
    PaymentTransactionJpaEntity toJpaEntity(PaymentTransaction tx);

    @Mapping(target = "id", expression = "java(az.unibank.smartorder.payment.domain.model.valueobject.PaymentTransactionId.of(entity.getId()))")
    @Mapping(target = "paymentId", expression = "java(az.unibank.smartorder.payment.domain.model.valueobject.PaymentId.of(entity.getPayment().getId()))")
    PaymentTransaction toDomainModel(PaymentTransactionJpaEntity entity);
}
