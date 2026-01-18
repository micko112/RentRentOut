package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.CreateAdRequestDto;


import org.landm.dto.ad.UpdateAdRequestDto;
import org.landm.service.AdService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<AdDto> createAd(@Valid @RequestBody CreateAdRequestDto req,
                                            @RequestHeader("Authorization") String authHeader){
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) auth.getPrincipal();
//        
//        long userId = user.getId();
        
        long userId = (long) auth.getPrincipal();
        
        return new ResponseEntity<>(adService.create(req, userId), HttpStatus.CREATED);
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

    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable long id,
                                          @Valid @RequestBody UpdateAdRequestDto req,
                                          @RequestHeader("Authorization") String authHeader){
        AdDto updatedAd = adService.updateAd(req, id, authHeader);
        return ResponseEntity.ok(updatedAd);
    }
}
