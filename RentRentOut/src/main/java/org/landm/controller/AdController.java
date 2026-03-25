package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.ad.*;
import org.landm.service.AdService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAdById(@PathVariable Long id, Authentication auth) {
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            Long userId = Long.parseLong(auth.getName());
            return ResponseEntity.ok(adService.getAdById(id, userId));
        }
        return ResponseEntity.ok(adService.getAdById(id));
    }

    @GetMapping()
    public ResponseEntity<Page<AdPreviewDto>> getAllAds(Pageable pageable, Authentication auth) {
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            Long userId = Long.parseLong(auth.getName());
            return ResponseEntity.ok(adService.getAllActiveAds(pageable, userId));
        }
        return ResponseEntity.ok(adService.getAllActiveAds(pageable));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AdDto> createAd(@Valid @RequestBody CreateAdRequestDto req,
                                          Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(adService.create(req, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable Long id,
                                          @Valid @RequestBody UpdateAdRequestDto req,
                                          Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        AdDto updatedAd = adService.updateAd(req, id, userId);
        return ResponseEntity.ok(updatedAd);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{adId}")
    public ResponseEntity<String> deleteAd(@PathVariable Long adId, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return new ResponseEntity<>(adService.deleteAd(adId, userId), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdPreviewDto>> searchAd(AdSearchCriteriaDto criteria, Pageable pageable,
                                                        Authentication auth) {
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            Long userId = Long.parseLong(auth.getName());
            return ResponseEntity.ok(adService.search(criteria, pageable, userId));
        }
        return ResponseEntity.ok(adService.search(criteria, pageable));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<Page<AdPreviewDto>> getMyAds(Authentication auth, Pageable pageable) {
        Long userId = Long.parseLong(auth.getName());
        Page<AdPreviewDto> results = adService.findAllByUser(pageable, userId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AdPreviewDto>> getAdsByUser(@PathVariable Long userId, Pageable pageable) {
        Page<AdPreviewDto> results = adService.findAllActiveByUser(pageable, userId);
        return ResponseEntity.ok(results);
    }

    // Ad Views

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{adId}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long adId, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        adService.recordView(adId, userId);
        return ResponseEntity.ok().build();
    }

    // Saved Ads

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{adId}/save")
    public ResponseEntity<Void> saveAd(@PathVariable Long adId, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        adService.saveAd(adId, userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{adId}/save")
    public ResponseEntity<Void> unsaveAd(@PathVariable Long adId, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        adService.unsaveAd(adId, userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{adId}/saved-status")
    public ResponseEntity<Map<String, Boolean>> getSavedStatus(@PathVariable Long adId, Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        boolean saved = adService.isSaved(adId, userId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me/saved")
    public ResponseEntity<Page<AdPreviewDto>> getSavedAds(Authentication auth, Pageable pageable) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(adService.getSavedAds(userId, pageable));
    }

}
