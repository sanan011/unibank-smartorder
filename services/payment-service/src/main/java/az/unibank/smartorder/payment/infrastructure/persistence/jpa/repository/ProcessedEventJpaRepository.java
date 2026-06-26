package az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository;

import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
