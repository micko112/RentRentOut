package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.ad.*;


import org.landm.service.AdService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAdById(@PathVariable long id){
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

        long userId = Long.parseLong(auth.getName());
        
        return new ResponseEntity<>(adService.create(req, userId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable long id,
                                          @Valid @RequestBody UpdateAdRequestDto req,
                                          Authentication auth){
        long userId = Long.parseLong(auth.getName());
        AdDto updatedAd = adService.updateAd(req, id, userId);
        return ResponseEntity.ok(updatedAd);
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{adId}")
    public ResponseEntity<String> deleteAd(@PathVariable long adId, Authentication auth){
    	long userId = Long.parseLong(auth.getName());
    	return new ResponseEntity<>(adService.deleteAd(adId, userId), HttpStatus.OK);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<AdPreviewDto>> searchAd(AdSearchCriteriaDto criteria, Pageable pageable){
        Page<AdPreviewDto> results = adService.search(criteria, pageable);
        return ResponseEntity.ok(results);
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<Page<AdPreviewDto>> getMyAds(Authentication auth, Pageable pageable){
        long userId = Long.parseLong(auth.getName());
        Page<AdPreviewDto> results = adService.findAllByUser(pageable, userId);
        return ResponseEntity.ok(results);
    }

}
