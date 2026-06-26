package az.unibank.smartorder.order.infrastructure.persistence.jpa.adapter;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper.ProductPersistenceMapper;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.getValue())
                .map(productPersistenceMapper::toDomainModel);
    }

    @Override
    public Optional<Product> findByIdWithPessimisticLock(ProductId id) {
        return productJpaRepository.findByIdWithPessimisticLock(id.getValue())
                .map(productPersistenceMapper::toDomainModel);
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = productPersistenceMapper.toJpaEntity(product);
        ProductJpaEntity saved = productJpaRepository.save(entity);
        return productPersistenceMapper.toDomainModel(saved);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAll(pageable)
                .map(productPersistenceMapper::toDomainModel);
    }
}
