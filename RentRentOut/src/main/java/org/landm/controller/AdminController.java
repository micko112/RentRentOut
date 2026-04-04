package org.landm.controller;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.admin.AdReportDto;
import org.landm.dto.admin.UserCreditSummaryDto;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── Ads ────────────────────────────────────────────────

    @GetMapping("/ads")
    public ResponseEntity<Page<AdPreviewDto>> getAllAds(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllAds(search, status, pageable));
    }

    @PatchMapping("/ads/{adId}/suspend")
    public ResponseEntity<String> suspendAd(@PathVariable Long adId) {
        return ResponseEntity.ok(adminService.suspendAd(adId));
    }

    @PatchMapping("/ads/{adId}/unsuspend")
    public ResponseEntity<String> unsuspendAd(@PathVariable Long adId) {
        return ResponseEntity.ok(adminService.unsuspendAd(adId));
    }

    @DeleteMapping("/ads/{adId}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long adId) {
        adminService.deleteAd(adId);
        return ResponseEntity.noContent().build();
    }

    // ── Users ──────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(search, pageable));
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<String> toggleUserEnabled(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserEnabled(id));
    }

    // ── Contracts ──────────────────────────────────────────

    @GetMapping("/contracts")
    public ResponseEntity<Page<RentalContractDto>> getAllContracts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllContracts(pageable));
    }

    // ── Stats ──────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ── Credits ────────────────────────────────────────────

    @GetMapping("/credits")
    public ResponseEntity<Page<UserCreditSummaryDto>> getUserCreditSummaries(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getUserCreditSummaries(search, pageable));
    }

    // ── Reports ────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<Page<AdReportDto>> getReports(
            @RequestParam(defaultValue = "false") boolean onlyUnreviewed,
            @PageableDefault(size = 25) Pageable pageable) {
        return ResponseEntity.ok(adminService.getReports(onlyUnreviewed, pageable));
    }

    @PatchMapping("/reports/{id}/reviewed")
    public ResponseEntity<Void> markReportReviewed(@PathVariable Long id) {
        adminService.markReportReviewed(id);
        return ResponseEntity.ok().build();
    }
}
