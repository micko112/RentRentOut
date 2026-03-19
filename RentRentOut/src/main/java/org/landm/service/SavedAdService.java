package org.landm.service;

import org.landm.dto.ad.AdPreviewDto;

import java.util.List;

public interface SavedAdService {
    void save(Long userId, Long adId);
    void unsave(Long userId, Long adId);
    List<AdPreviewDto> getSavedAds(Long userId);
    boolean isSaved(Long userId, Long adId);
}
