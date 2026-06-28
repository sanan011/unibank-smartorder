package az.unibank.smartorder.order.infrastructure.messaging.adapter;

import az.unibank.smartorder.order.application.dto.DlqMessage;
import az.unibank.smartorder.order.domain.port.outbound.DlqPort;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
public class RabbitMqDlqAdapter implements DlqPort {

    private final RabbitTemplate rabbitTemplate;
    
    private static final List<String> DLQ_NAMES = List.of(
            "order.payment.processed.dlq",
            "order.payment.failed.dlq"
    );

    @Override
    public List<DlqMessage> getMessages() {
        List<DlqMessage> dlqMessages = new ArrayList<>();
        for (String dlqName : DLQ_NAMES) {
            dlqMessages.addAll(getMessagesFromQueue(dlqName));
        }
        return dlqMessages;
    }

    private List<DlqMessage> getMessagesFromQueue(String dlqName) {
        return rabbitTemplate.execute(channel -> {
            List<DlqMessage> messages = new ArrayList<>();
            List<Long> deliveryTags = new ArrayList<>();
            
            try {
                long count = channel.messageCount(dlqName);
                for (int i = 0; i < count; i++) {
                    GetResponse response = channel.basicGet(dlqName, false);
                    if (response != null) {
                        deliveryTags.add(response.getEnvelope().getDeliveryTag());
                        messages.add(buildDlqMessage(dlqName, response));
                    } else {
                        break;
                    }
                }
            } finally {
                for (Long tag : deliveryTags) {
                    channel.basicReject(tag, true);
                }
            }
            return messages;
        });
    }

    @Override
    public void retryMessage(String messageId) {
        for (String dlqName : DLQ_NAMES) {
            Boolean found = rabbitTemplate.execute(channel -> {
                long count = channel.messageCount(dlqName);
                List<Long> unacked = new ArrayList<>();
                boolean messageFound = false;
                
                try {
                    for (int i = 0; i < count; i++) {
                        GetResponse response = channel.basicGet(dlqName, false);
                        if (response != null) {
                            String id = generateId(response);
                            if (id.equals(messageId)) {
                                Map<String, Object> headers = response.getProps().getHeaders();
                                
                                String originalExchange = "";
                                String originalRoutingKey = response.getEnvelope().getRoutingKey();
                                
                                if (headers != null && headers.containsKey("x-death")) {
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> xDeathList = (List<Map<String, Object>>) headers.get("x-death");
                                    if (xDeathList != null && !xDeathList.isEmpty()) {
                                        Map<String, Object> deathHeader = xDeathList.get(0);
                                        if (deathHeader.containsKey("exchange")) {
                                            originalExchange = deathHeader.get("exchange").toString();
                                        }
                                        if (deathHeader.containsKey("routing-keys")) {
                                            @SuppressWarnings("unchecked")
                                            List<String> routingKeys = (List<String>) deathHeader.get("routing-keys");
                                            if (routingKeys != null && !routingKeys.isEmpty()) {
                                                originalRoutingKey = routingKeys.get(0);
                                            }
                                        }
                                    }
                                }

                                Map<String, Object> newHeaders = headers != null ? new HashMap<>(headers) : new HashMap<>();
                                newHeaders.remove("x-death");
                                
                                AMQP.BasicProperties newProps = response.getProps().builder()
                                        .headers(newHeaders)
                                        .build();
                                
                                channel.basicPublish(originalExchange, originalRoutingKey, newProps, response.getBody());
                                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                                
                                log.info("Retried message {} from {} to exchange '{}' routingKey '{}'", messageId, dlqName, originalExchange, originalRoutingKey);
                                messageFound = true;
                                break;
                            } else {
                                unacked.add(response.getEnvelope().getDeliveryTag());
                            }
                        } else {
                            break;
                        }
                    }
                } finally {
                    for (Long tag : unacked) {
                        channel.basicReject(tag, true);
                    }
                }
                return messageFound;
            });
            
            if (Boolean.TRUE.equals(found)) {
                return;
            }
        }
        log.warn("Message {} not found for retry in any DLQ", messageId);
    }

    @Override
    public void discardMessage(String messageId) {
        for (String dlqName : DLQ_NAMES) {
            Boolean found = rabbitTemplate.execute(channel -> {
                long count = channel.messageCount(dlqName);
                List<Long> unacked = new ArrayList<>();
                boolean messageFound = false;
                
                try {
                    for (int i = 0; i < count; i++) {
                        GetResponse response = channel.basicGet(dlqName, false);
                        if (response != null) {
                            String id = generateId(response);
                            if (id.equals(messageId)) {
                                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                                log.info("Discarded message {} from {}", messageId, dlqName);
                                messageFound = true;
                                break;
                            } else {
                                unacked.add(response.getEnvelope().getDeliveryTag());
                            }
                        } else {
                            break;
                        }
                    }
                } finally {
                    for (Long tag : unacked) {
                        channel.basicReject(tag, true);
                    }
                }
                return messageFound;
            });
            
            if (Boolean.TRUE.equals(found)) {
                return;
            }
        }
        log.warn("Message {} not found for discard in any DLQ", messageId);
    }

    private DlqMessage buildDlqMessage(String dlqName, GetResponse response) {
        String payload = new String(response.getBody(), StandardCharsets.UTF_8);
        String reason = extractReason(response);
        
        return DlqMessage.builder()
                .id(generateId(response))
                .queueName(dlqName)
                .payload(payload)
                .reason(reason)
                .build();
    }

    private String generateId(GetResponse response) {
        String messageId = response.getProps().getMessageId();
        if (messageId != null && !messageId.isBlank()) {
            return messageId;
        }
        return DigestUtils.md5DigestAsHex(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private String extractReason(GetResponse response) {
        Map<String, Object> headers = response.getProps().getHeaders();
        if (headers == null) return "Unknown";
        
        List<Map<String, Object>> xDeathList = (List<Map<String, Object>>) headers.get("x-death");
        if (xDeathList == null || xDeathList.isEmpty()) return "Unknown";
        
        Map<String, Object> deathHeader = xDeathList.get(0);
        Object reasonObj = deathHeader.get("reason");
        return reasonObj != null ? reasonObj.toString() : "Unknown";
    }
}
