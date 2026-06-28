package az.unibank.smartorder.order.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DlqMessage {
    String id;
    String queueName;
    String payload;
    String reason;
}
