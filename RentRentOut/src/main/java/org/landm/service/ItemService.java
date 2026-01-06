package org.landm.service;

import org.landm.dto.CreateItemRequestDto;
import org.landm.dto.ItemDto;

public interface ItemService {

    public ItemDto create(Long userId, CreateItemRequestDto req);

   // ItemDto create(CreateItemRequestDto req);
}
