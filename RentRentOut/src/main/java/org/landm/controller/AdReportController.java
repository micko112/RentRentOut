package org.landm.controller;

import org.landm.entity.Ad;
import org.landm.entity.AdReport;
import org.landm.entity.User;
import org.landm.repository.AdRepository;
import org.landm.repository.AdReportRepository;
import org.landm.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ads")
public class AdReportController {

    private final AdRepository adRepository;
    private final AdReportRepository adReportRepository;
    private final UserRepository userRepository;

    public AdReportController(AdRepository adRepository,
                               AdReportRepository adReportRepository,
                               UserRepository userRepository) {
        this.adRepository = adRepository;
        this.adReportRepository = adReportRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{adId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> reportAd(@PathVariable Long adId,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Oglas nije pronađen."));

        if (ad.getOwner().getId().equals(userId)) {
            return ResponseEntity.badRequest().body("Ne možete prijaviti sopstveni oglas.");
        }

        if (adReportRepository.existsByAdIdAndReporterId(adId, userId)) {
            return ResponseEntity.badRequest().body("Već ste prijavili ovaj oglas.");
        }

        String reason = body.getOrDefault("reason", "").trim();
        if (reason.isBlank()) {
            return ResponseEntity.badRequest().body("Razlog prijave je obavezan.");
        }
        String note = body.getOrDefault("note", "").trim();
        if (note.length() > 500) note = note.substring(0, 500);

        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen."));

        adReportRepository.save(new AdReport(ad, reporter, reason, note));
        return ResponseEntity.ok("Prijava je uspešno poslata. Hvala vam!");
    }
}
