package az.unibank.smartorder.order.infrastructure.persistence.jpa.adapter;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper.OrderPersistenceMapper;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderPersistenceMapper.toJpaEntity(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return orderPersistenceMapper.toDomainModel(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return orderJpaRepository.findById(id.getValue())
                .map(orderPersistenceMapper::toDomainModel);
    }

    @Override
    public Page<Order> findAllByCustomerId(CustomerId customerId, String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
            return orderJpaRepository.findByCustomerId(customerId.getValue(), pageable)
                    .map(orderPersistenceMapper::toDomainModel);
        }
        return orderJpaRepository.findByCustomerIdAndStatus(customerId.getValue(), status, pageable)
                .map(orderPersistenceMapper::toDomainModel);
    }
}
