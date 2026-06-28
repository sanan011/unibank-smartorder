package az.unibank.smartorder.order.application.service;

import az.unibank.smartorder.order.application.dto.DlqMessage;
import az.unibank.smartorder.order.domain.port.inbound.DlqAdminUseCase;
import az.unibank.smartorder.order.domain.port.outbound.DlqPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
public class DlqAdminService implements DlqAdminUseCase {

    private final DlqPort dlqPort;

    @Override
    public List<DlqMessage> listMessages() {
        log.info("Listing all messages from known DLQs");
        return dlqPort.getMessages();
    }

    @Override
    public void retryMessage(String messageId) {
        log.info("Retrying DLQ message with id: {}", messageId);
        dlqPort.retryMessage(messageId);
    }

    @Override
    public void discardMessage(String messageId) {
        log.info("Discarding DLQ message with id: {}", messageId);
        dlqPort.discardMessage(messageId);
    }
}
