package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.dto.DlqMessage;

import java.util.List;

public interface DlqAdminUseCase {
    List<DlqMessage> listMessages();
    void retryMessage(String messageId);
    void discardMessage(String messageId);
}
