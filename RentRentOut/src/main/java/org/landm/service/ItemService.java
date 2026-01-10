package org.landm.service;

import org.landm.dto.requestDto.CreateItemRequestDto;
import org.landm.dto.ItemDto;

public interface ItemService {

    public ItemDto create(CreateItemRequestDto req, String authHeader);

   // ItemDto create(CreateItemRequestDto req);
}
