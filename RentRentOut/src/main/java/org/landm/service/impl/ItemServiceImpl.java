package org.landm.service.impl;

import org.landm.dto.requestDto.CreateItemRequestDto;
import org.landm.dto.ItemDto;
import org.landm.entity.Category;
import org.landm.entity.Item;
import org.landm.entity.Location;
import org.landm.entity.User;
import org.landm.mapper.ItemMapper;
import org.landm.mapper.LocationMapper;
import org.landm.repository.CategoryRepository;
import org.landm.repository.ItemRepository;
import org.landm.repository.LocationRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.ItemService;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final LocationMapper locationMapper;

    private final JwtUtil jwtUtil;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           ItemMapper itemMapper, LocationMapper locationMapper, CategoryRepository categoryRepository, LocationRepository locationRepository, JwtUtil jwtUtil) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.categoryRepository = categoryRepository;
        this.locationMapper = locationMapper;
        this.locationRepository = locationRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ItemDto create(CreateItemRequestDto req, long userId) {

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
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));
        Location location = locationMapper.toEntity(req.getLocation());
        locationRepository.save(location);
        Item itemToCreate = new Item(
            req.getName(),
                req.getPrice(),
                req.getDays(),
                req.getDescription(),
                owner,
                category,
                location
        );
        return itemMapper.toDto(itemRepository.save(itemToCreate));
    }
}
