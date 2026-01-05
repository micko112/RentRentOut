package org.landm.mapper;

import org.landm.dto.ItemDto;
import org.landm.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {
    public ItemDto toDto(Item item){
        return new ItemDto(
                item.getItemName(),
                item.getItemPrice(),
                item.getDays()
        );
    }

}
