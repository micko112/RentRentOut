package org.landm.service;

import java.time.LocalDate;
import java.util.List;

import org.landm.dto.ad.*;
import org.landm.entity.RentalContract;
import org.landm.entity.Enums.AdStatus;
import org.landm.helper.DateInterval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdService {

    public AdDto create(CreateAdRequestDto req, Long userId);
    public AdDto getAdById(Long id);
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable);
    public AdDto updateAd(UpdateAdRequestDto req, Long id, Long userId);
   // ItemDto create(CreateItemRequestDto req);
    public String deleteAd(Long adId, Long userId);
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable);
    public Page<AdPreviewDto> findAll(Pageable pageable);
    public Page<AdPreviewDto> findAllByUser(Pageable pageable, Long userId);

    public AdDto updateAdStatus(Long adId, AdStatus newStatus, Long userId);

}
