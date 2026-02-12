package org.landm.service;

import java.util.List;

import org.landm.dto.ad.*;
import org.landm.entity.RentalContract;
import org.landm.entity.Enums.AdStatus;
import org.landm.helper.DateInterval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdService {

    public AdDto create(CreateAdRequestDto req, long userId);
    public AdDto getAdById(long id);
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable);
    public AdDto updateAd(UpdateAdRequestDto req, long id, long userId);
   // ItemDto create(CreateItemRequestDto req);
    public String deleteAd(long adId, long userId);
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable);
    public Page<AdPreviewDto> findAll(Pageable pageable);
    public Page<AdPreviewDto> findAllByUser(Pageable pageable, long userId);
}
