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
    public AdDto getAdById(Long id, Long userId);
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable);
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable, Long userId);
    public AdDto updateAd(UpdateAdRequestDto req, Long id, Long userId);
    public String deleteAd(Long adId, Long userId);
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable);
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable, Long userId);
    public Page<AdPreviewDto> findAll(Pageable pageable);
    public Page<AdPreviewDto> findAllByUser(Pageable pageable, Long userId);
    public Page<AdPreviewDto> findAllActiveByUser(Pageable pageable, Long userId);

    // Ad Views
    void recordView(Long adId, Long userId);

    // Saved Ads
    void saveAd(Long adId, Long userId);
    void unsaveAd(Long adId, Long userId);
    boolean isSaved(Long adId, Long userId);
    Page<AdPreviewDto> getSavedAds(Long userId, Pageable pageable);

}
