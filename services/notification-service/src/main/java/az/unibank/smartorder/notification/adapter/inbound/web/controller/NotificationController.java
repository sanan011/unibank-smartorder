package az.unibank.smartorder.notification.adapter.inbound.web.controller;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.port.inbound.QueryNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final QueryNotificationUseCase queryUseCase;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotificationsByCustomerId(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryUseCase.getNotificationsByCustomerId(customerId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable UUID id) {
        return ResponseEntity.ok(queryUseCase.getNotificationById(id));
    }
}
