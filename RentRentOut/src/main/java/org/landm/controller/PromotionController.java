package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.promotion.*;
import org.landm.entity.Enums.PromotionType;
import org.landm.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /** GET /api/promotions/packages — lista paketa sa cenama (javno) */
    @GetMapping("/packages")
    public ResponseEntity<List<PromotionPackageDto>> getPackages() {
        return ResponseEntity.ok(promotionService.getPackages());
    }

    /** POST /api/promotions/activate — aktivacija promocije */
    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivePromotionDto> activate(
            @Valid @RequestBody ActivatePromotionRequest request,
            @AuthenticationPrincipal Long userId) {
        ActivePromotionDto dto = promotionService.activate(request.getAdId(), request.getPromotionType(), userId);
        return ResponseEntity.ok(dto);
    }

    /** GET /api/promotions/ad/{adId} — aktivne promocije za oglas */
    @GetMapping("/ad/{adId}")
    public ResponseEntity<List<ActivePromotionDto>> getActiveForAd(@PathVariable Long adId) {
        return ResponseEntity.ok(promotionService.getActivePromotions(adId));
    }

    /** POST /api/promotions/renew/{adId} — besplatna obnova oglasa */
    @PostMapping("/renew/{adId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> renewAd(
            @PathVariable Long adId,
            @AuthenticationPrincipal Long userId) {
        promotionService.renewAd(adId, userId);
        return ResponseEntity.ok().build();
    }

    /** GET /api/promotions/credit — stanje kredita ulogovanog korisnika */
    @GetMapping("/credit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreditBalanceDto> getCreditBalance(
            @AuthenticationPrincipal Long userId) {
        BigDecimal balance = promotionService.getCreditBalance(userId);
        return ResponseEntity.ok(new CreditBalanceDto(balance));
    }

    /** GET /api/promotions/credit/history — istorija transakcija */
    @GetMapping("/credit/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CreditTransactionDto>> getCreditHistory(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(promotionService.getCreditHistory(userId, pageable));
    }

    /** GET /api/promotions/admin/transactions — sve kreditne transakcije (admin) */
    @GetMapping("/admin/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminCreditTransactionDto>> getAllTransactions(
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(promotionService.getAllTransactions(pageable));
    }

    /** POST /api/promotions/admin/credit — admin dodaje kredit korisniku */
    @PostMapping("/admin/credit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addCredit(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Dopuna kredita") String description) {
        promotionService.addCredit(userId, amount, description);
        return ResponseEntity.ok().build();
    }
}
