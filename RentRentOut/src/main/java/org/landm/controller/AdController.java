package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.ad.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.service.AdService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAdById(@PathVariable Long id){
        AdDto adDto = adService.getAdById(id);
        return  ResponseEntity.ok(adDto);
    }
    @GetMapping()
    public ResponseEntity<Page<AdPreviewDto>> getAllAds(Pageable pageable){
        Page<AdPreviewDto> adsPage = adService.getAllActiveAds(pageable);
        return ResponseEntity.ok(adsPage);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<AdDto> createAd(@Valid @RequestBody CreateAdRequestDto req,
                                            Authentication auth){

        Long userId = Long.parseLong(auth.getName());
        
        return new ResponseEntity<>(adService.create(req, userId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable Long id,
                                          @Valid @RequestBody UpdateAdRequestDto req,
                                          Authentication auth){
        Long userId = Long.parseLong(auth.getName());
        AdDto updatedAd = adService.updateAd(req, id, userId);
        return ResponseEntity.ok(updatedAd);
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{adId}")
    public ResponseEntity<String> deleteAd(@PathVariable Long adId, Authentication auth){
    	Long userId = Long.parseLong(auth.getName());
    	return new ResponseEntity<>(adService.deleteAd(adId, userId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{adId}/status")
    public ResponseEntity<AdDto> updateAdStatus(@PathVariable Long adId,
                                                @RequestParam AdStatus status,
                                                Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(adService.updateAdStatus(adId, status, userId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<AdPreviewDto>> searchAd(AdSearchCriteriaDto criteria, Pageable pageable){
        Page<AdPreviewDto> results = adService.search(criteria, pageable);
        return ResponseEntity.ok(results);
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<Page<AdPreviewDto>> getMyAds(Authentication auth, Pageable pageable){
        Long userId = Long.parseLong(auth.getName());
        Page<AdPreviewDto> results = adService.findAllByUser(pageable, userId);
        return ResponseEntity.ok(results);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AdPreviewDto>> getAdsByUser(@PathVariable Long userId, Pageable pageable){

        Page<AdPreviewDto> results = adService.findAllByUser(pageable, userId);
        return ResponseEntity.ok(results);
    }

}
