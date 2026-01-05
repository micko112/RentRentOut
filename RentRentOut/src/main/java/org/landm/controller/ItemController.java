package org.landm.controller;

import org.landm.service.ItemService;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

}
