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
    @PostMapping("create")
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody CreateItemRequestDto req,
                                              @RequestHeader("Authorization") String authHeader){
        return new ResponseEntity<>(itemService.create(req, authHeader), HttpStatus.CREATED);
    }



}
