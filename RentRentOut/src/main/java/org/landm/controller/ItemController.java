package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.CreateItemRequestDto;
import org.landm.dto.ItemDto;
import org.landm.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")

public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody CreateItemRequestDto req){
        Long userId=1L; // ovde posle treba da uzme id od kreatora
        return new ResponseEntity<>(itemService.create(userId, req), HttpStatus.CREATED);
    }



}
