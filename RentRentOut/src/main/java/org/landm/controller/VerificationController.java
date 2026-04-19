package org.landm.controller;

import org.landm.dto.verification.VerificationStatusDto;
import org.landm.service.IdentityVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification")
@PreAuthorize("isAuthenticated()")
public class VerificationController {

    private final IdentityVerificationService service;

    public VerificationController(IdentityVerificationService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public ResponseEntity<VerificationStatusDto> submit(
            @AuthenticationPrincipal Long userId,
            @RequestParam("docFront") MultipartFile docFront,
            @RequestParam(value = "docBack", required = false) MultipartFile docBack,
            @RequestParam("selfie") MultipartFile selfie) {
        return ResponseEntity.ok(service.submit(userId, docFront, docBack, selfie));
    }

    @GetMapping("/status")
    public ResponseEntity<VerificationStatusDto> getMyStatus(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(service.getMyStatus(userId));
    }
}
