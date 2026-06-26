package az.unibank.smartorder.notification.infrastructure.persistence.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationDocument {
    @Id
    private UUID id;
    private UUID orderId;
    private UUID customerId;
    private String type;
    private String status;
    private String message;
    private Instant occurredAt;
    private Instant createdAt;
}
