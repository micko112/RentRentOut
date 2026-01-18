package org.landm.service.impl;

import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.CreateAdRequestDto;

import org.landm.entity.Ad;
import org.landm.entity.Category;
import org.landm.entity.Enums.AdStatus;

import org.landm.entity.Location;
import org.landm.entity.User;
import org.landm.mapper.AdMapper;
import org.landm.mapper.LocationMapper;
import org.landm.repository.CategoryRepository;
import org.landm.repository.AdRepository;
import org.landm.repository.LocationRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdServiceImpl implements AdService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final LocationMapper locationMapper;

    private final JwtUtil jwtUtil;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public AdServiceImpl(AdRepository adRepository, UserRepository userRepository,
                         AdMapper adMapper, LocationMapper locationMapper, CategoryRepository categoryRepository, LocationRepository locationRepository, JwtUtil jwtUtil) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.adMapper = adMapper;
        this.categoryRepository = categoryRepository;
        this.locationMapper = locationMapper;
        this.locationRepository = locationRepository;
        this.jwtUtil = jwtUtil;
    }

//    @Override
//    public ItemDto create(CreateItemRequestDto req, long userId) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//
//    }

    @Override
    public AdDto create(CreateAdRequestDto req, long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));
        Location location = locationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));
        Ad adToCreate = new Ad(
                req.getTitle(),
                req.getDescription(),
                req.getPrice(),
                req.getPriceInterval(),
                owner,
                category,
                location,
                AdStatus.ACTIVE,
                req.getTotalQuantity(),
                req.getImages()
                );
        return adMapper.toDto(adRepository.save(adToCreate));
    }

    @Override
    public AdDto getAdById(long id) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));
        return adMapper.toDto(ad);
    }

    @Override
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable) {

        Page<Ad> adPage = adRepository.findAllByAdStatus(AdStatus.ACTIVE, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }


}
