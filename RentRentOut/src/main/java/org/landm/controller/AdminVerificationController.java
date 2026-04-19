package org.landm.controller;

import org.landm.dto.verification.AdminVerificationDetailsDto;
import org.landm.dto.verification.AdminVerificationDto;
import org.landm.service.IdentityVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/verifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationController {

    private final IdentityVerificationService service;

    public AdminVerificationController(IdentityVerificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<AdminVerificationDto>> list(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.listForAdmin(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminVerificationDetailsDto> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetailsForAdmin(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id,
                                         @AuthenticationPrincipal Long adminUserId) {
        service.approve(id, adminUserId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id,
                                        @AuthenticationPrincipal Long adminUserId,
                                        @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "").trim();
        service.reject(id, adminUserId, reason);
        return ResponseEntity.ok().build();
    }
}
