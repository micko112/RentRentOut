package org.landm.service.impl;

import org.landm.dto.CreateItemRequestDto;
import org.landm.dto.ItemDto;
import org.landm.entity.Item;
import org.landm.entity.User;
import org.landm.mapper.ItemMapper;
import org.landm.repository.ItemRepository;
import org.landm.repository.UserRepository;
import org.landm.service.ItemService;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
    }


    @Override
    public ItemDto create(Long userId, CreateItemRequestDto req) {

        User owner = userRepository.findById(userId).orElseThrow(()->new RuntimeException("user not found"));

        Item itemToCreate = new Item(
            req.getName(),
                req.getPrice(),
                req.getDays(),
                req.getDescription(),
                owner
        );
        Item savedItem = itemRepository.save(itemToCreate);
        return itemMapper.toDto(savedItem);
    }
}
