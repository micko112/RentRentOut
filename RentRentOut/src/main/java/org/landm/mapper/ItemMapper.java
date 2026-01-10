package org.landm.mapper;

import org.landm.dto.ItemDto;
import org.landm.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {
    public ItemDto toDto(Item item){
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setPrice(item.getPrice());
        itemDto.setDescription(item.getDescription());
        itemDto.setDays(item.getDays());
        itemDto.setCategoryId(item.getCategory().getId());
    return itemDto;
    }

}
