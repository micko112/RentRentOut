package org.landm.controller;

import org.landm.dto.ad.AdPreviewDto;
import org.landm.service.SavedAdService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-ads")
public class SavedAdController {

    private final SavedAdService savedAdService;

    public SavedAdController(SavedAdService savedAdService) {
        this.savedAdService = savedAdService;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{adId}")
    public ResponseEntity<Void> save(@PathVariable Long adId, Authentication auth) {
        savedAdService.save(Long.parseLong(auth.getName()), adId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{adId}")
    public ResponseEntity<Void> unsave(@PathVariable Long adId, Authentication auth) {
        savedAdService.unsave(Long.parseLong(auth.getName()), adId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<AdPreviewDto>> getSavedAds(Authentication auth) {
        return ResponseEntity.ok(savedAdService.getSavedAds(Long.parseLong(auth.getName())));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/is-saved/{adId}")
    public ResponseEntity<Boolean> isSaved(@PathVariable Long adId, Authentication auth) {
        return ResponseEntity.ok(savedAdService.isSaved(Long.parseLong(auth.getName()), adId));
    }
}
