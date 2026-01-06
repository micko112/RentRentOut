package org.landm.service.impl;

import org.landm.dto.CreateItemRequestDto;
import org.landm.dto.ItemDto;
import org.landm.entity.Item;
import org.landm.entity.User;
import org.landm.mapper.ItemMapper;
import org.landm.repository.ItemRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.ItemService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final JwtUtil jwtUtil;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           ItemMapper itemMapper, JwtUtil jwtUtil) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ItemDto create(CreateItemRequestDto req, String authHeader) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//
//        User owner = userRepository.findByEmail(email);
//
//        if (owner == null) throw new RuntimeException("No user found!");
//
//        Item itemToCreate = new Item(
//            req.getName(),
//                req.getPrice(),
//                req.getDays(),
//                req.getDescription(),
//                owner.getUserId()
//        );
        long userId = jwtUtil.extractUserId(authHeader.substring(7));
        User tempUser = new User();
        tempUser.setUserId(userId);

        Item itemToCreate = new Item(
            req.getName(),
                req.getPrice(),
                req.getDays(),
                req.getDescription(),
                tempUser
        );

        Item savedItem = itemRepository.save(itemToCreate);
        return itemMapper.toDto(savedItem);
    }
}
