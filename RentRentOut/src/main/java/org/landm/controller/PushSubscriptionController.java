package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.push.SavePushSubscriptionDto;
import org.landm.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // Mobile (FCM) — token se čuva u istom push_subscription tabeli sa "fcm:" prefiksom.
    // NotificationServiceImpl kad šalje push mora detektovati "fcm:" prefix i koristiti Firebase Admin SDK.
    @PostMapping("/mobile-register")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> mobileRegister(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String token = body.get("token");
        String platform = body.getOrDefault("platform", "android");
        if (token == null || token.isBlank()) return ResponseEntity.badRequest().build();
        Long userId = Long.parseLong(auth.getName());
        notificationService.savePushSubscription("fcm:" + token, platform, "", userId);
        return ResponseEntity.ok().build();
    }
}
