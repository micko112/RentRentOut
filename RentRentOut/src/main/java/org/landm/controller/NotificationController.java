package org.landm.controller;

import org.landm.dto.notification.NotificationDto;
import org.landm.service.NotificationPersistenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationPersistenceService notificationService;

    public NotificationController(NotificationPersistenceService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAll(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(notificationService.getForUser(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markOneAsRead(@PathVariable Long id, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        notificationService.markOneAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
