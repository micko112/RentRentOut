package org.landm.controller;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.user.UserDto;
import org.landm.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping("/ads/{adId}/suspend")
    public ResponseEntity<String> suspendAd(@PathVariable Long adId) {
        String response = adminService.suspendAd(adId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/ads/{adId}/unsuspend")
    public ResponseEntity<String> unsuspendAd(@PathVariable Long adId) {
        String response = adminService.unsuspendAd(adId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<String> toggleUserEnabled(@PathVariable Long id) {
        String response = adminService.toggleUserEnabled(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/ads")
    public ResponseEntity<Page<AdPreviewDto>> getAllAds(@PageableDefault(size = 20) Pageable pageable) {
        Page<AdPreviewDto> ads = adminService.getAllAds(pageable);
        return ResponseEntity.ok(ads);
    }

    @GetMapping("/contracts")
    public ResponseEntity<Page<RentalContractDto>> getAllContracts(@PageableDefault(size = 20) Pageable pageable) {
        Page<RentalContractDto> contracts = adminService.getAllContracts(pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = adminService.getStats();
        return ResponseEntity.ok(stats);
    }
}
