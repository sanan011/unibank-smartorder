package az.unibank.smartorder.order.domain.port.outbound;

import az.unibank.smartorder.order.application.dto.DlqMessage;

import java.util.List;

public interface DlqPort {
    List<DlqMessage> getMessages();
    void retryMessage(String messageId);
    void discardMessage(String messageId);
}
