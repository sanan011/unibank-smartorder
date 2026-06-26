package az.unibank.smartorder.payment.infrastructure.persistence.jpa.adapter;

import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentRepository;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.mapper.PaymentPersistenceMapper;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentPersistenceMapper paymentPersistenceMapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentPersistenceMapper.toJpaEntity(payment);
        PaymentJpaEntity saved = paymentJpaRepository.save(entity);
        return paymentPersistenceMapper.toDomainModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(PaymentId id) {
        return paymentJpaRepository.findById(id.value())
                .map(paymentPersistenceMapper::toDomainModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(paymentPersistenceMapper::toDomainModel);
    }
}
