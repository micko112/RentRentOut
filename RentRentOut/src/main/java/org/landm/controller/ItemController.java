package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.requestDto.CreateItemRequestDto;
import org.landm.entity.User;
import org.landm.dto.ItemDto;
import org.landm.service.ItemService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody CreateItemRequestDto req,
                                              @RequestHeader("Authorization") String authHeader){
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) auth.getPrincipal();
//        
//        long userId = user.getId();
        
        long userId = (long) auth.getPrincipal();
        
        return new ResponseEntity<>(itemService.create(req, userId), HttpStatus.CREATED);
    }



}
