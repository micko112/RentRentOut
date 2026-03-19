package org.landm.controller;

import org.landm.dto.push.SavePushSubscriptionDto;
import org.landm.security.JwtUtil;
import org.landm.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
public class PushSubscriptionController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public PushSubscriptionController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestBody SavePushSubscriptionDto dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        notificationService.savePushSubscription(dto.getEndpoint(), dto.getP256dh(), dto.getAuth(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @RequestBody SavePushSubscriptionDto dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        notificationService.deletePushSubscription(dto.getEndpoint(), userId);
        return ResponseEntity.ok().build();
    }
}
