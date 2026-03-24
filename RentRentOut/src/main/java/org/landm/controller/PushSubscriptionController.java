package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.push.SavePushSubscriptionDto;
import org.landm.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
public class PushSubscriptionController {

    private final NotificationService notificationService;

    public PushSubscriptionController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> subscribe(
            @Valid @RequestBody SavePushSubscriptionDto dto,
            Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        notificationService.savePushSubscription(dto.getEndpoint(), dto.getP256dh(), dto.getAuth(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> unsubscribe(
            @Valid @RequestBody SavePushSubscriptionDto dto,
            Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        notificationService.deletePushSubscription(dto.getEndpoint(), userId);
        return ResponseEntity.ok().build();
    }
}
