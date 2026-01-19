package org.landm.service;

import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.CreateAdRequestDto;
import org.landm.dto.ad.UpdateAdRequestDto;
import org.landm.entity.Enums.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdService {

    public AdDto create(CreateAdRequestDto req, long userId);
    public AdDto getAdById(long id);
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable);
    public AdDto updateAd(UpdateAdRequestDto req, long id, long userId);
   // ItemDto create(CreateItemRequestDto req);
    public String deleteAd(long adId, long userId);
}
