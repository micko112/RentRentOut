package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.AdDto;
import org.landm.dto.requestDto.CreateAdRequestDto;


import org.landm.service.AdService;
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



}
