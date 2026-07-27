package org.landm.controller;

import org.landm.entity.Ad;
import org.landm.entity.AdReport;
import org.landm.entity.User;
import org.landm.repository.AdRepository;
import org.landm.repository.AdReportRepository;
import org.landm.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    public AdReportController(AdRepository adRepository,
                               AdReportRepository adReportRepository,
                               UserRepository userRepository,
                               MessageSource messageSource) {
        this.adRepository = adRepository;
        this.adReportRepository = adReportRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @PostMapping("/{adId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> reportAd(@PathVariable Long adId,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException(msg("error.ad.not_found")));

        if (ad.getOwner().getId().equals(userId)) {
            return ResponseEntity.badRequest().body(msg("error.report.cannot_report_own"));
        }

        if (adReportRepository.existsByAdIdAndReporterId(adId, userId)) {
            return ResponseEntity.badRequest().body(msg("error.report.already_reported"));
        }

        String reason = body.getOrDefault("reason", "").trim();
        if (reason.isBlank()) {
            return ResponseEntity.badRequest().body(msg("error.report.reason_required"));
        }
        String note = body.getOrDefault("note", "").trim();
        if (note.length() > 500) note = note.substring(0, 500);

        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(msg("error.user.not_found")));

        adReportRepository.save(new AdReport(ad, reporter, reason, note));
        return ResponseEntity.ok(msg("success.report.sent"));
    }
}
