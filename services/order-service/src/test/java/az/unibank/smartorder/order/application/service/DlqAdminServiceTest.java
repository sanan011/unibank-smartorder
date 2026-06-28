package az.unibank.smartorder.order.application.service;

import az.unibank.smartorder.order.application.dto.DlqMessage;
import az.unibank.smartorder.order.domain.port.outbound.DlqPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DlqAdminServiceTest {

    @Mock
    private DlqPort dlqPort;

    @InjectMocks
    private DlqAdminService dlqAdminService;

    private DlqMessage mockMessage;

    @BeforeEach
    void setUp() {
        mockMessage = DlqMessage.builder()
                .id("123")
                .queueName("test.dlq")
                .payload("{}")
                .reason("error")
                .build();
    }

    @Test
    void shouldListMessages() {
        when(dlqPort.getMessages()).thenReturn(List.of(mockMessage));
        
        List<DlqMessage> result = dlqAdminService.listMessages();
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("123");
        verify(dlqPort).getMessages();
    }

    @Test
    void shouldRetryMessage() {
        dlqAdminService.retryMessage("123");
        verify(dlqPort).retryMessage("123");
    }

    @Test
    void shouldDiscardMessage() {
        dlqAdminService.discardMessage("123");
        verify(dlqPort).discardMessage("123");
    }
}
