package az.unibank.smartorder.order.adapter.inbound.web.admin;

import az.unibank.smartorder.order.application.dto.DlqMessage;
import az.unibank.smartorder.order.domain.port.inbound.DlqAdminUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dlq/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
public class DlqAdminController {

    private final DlqAdminUseCase dlqAdminUseCase;

    @GetMapping
    public ResponseEntity<List<DlqMessage>> listMessages() {
        return ResponseEntity.ok(dlqAdminUseCase.listMessages());
    }

    @PostMapping("/{messageId}/retry")
    public ResponseEntity<Void> retryMessage(@PathVariable String messageId) {
        dlqAdminUseCase.retryMessage(messageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> discardMessage(@PathVariable String messageId) {
        dlqAdminUseCase.discardMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
