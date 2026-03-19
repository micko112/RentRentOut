package org.landm.service.impl;

import jakarta.transaction.Transactional;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.entity.Ad;
import org.landm.entity.SavedAd;
import org.landm.entity.User;
import org.landm.mapper.AdMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.SavedAdRepository;
import org.landm.repository.UserRepository;
import org.landm.service.SavedAdService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedAdServiceImpl implements SavedAdService {

    private final SavedAdRepository savedAdRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;

    public SavedAdServiceImpl(SavedAdRepository savedAdRepository,
                              UserRepository userRepository,
                              AdRepository adRepository,
                              AdMapper adMapper) {
        this.savedAdRepository = savedAdRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }

    @Override
    @Transactional
    public void save(Long userId, Long adId) {
        if (savedAdRepository.existsByUserIdAndAdId(userId, adId)) {
            throw new RuntimeException("Oglas je već sačuvan.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Oglas nije pronađen."));
        if (ad.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Ne možete sačuvati sopstveni oglas.");
        }
        savedAdRepository.save(new SavedAd(user, ad));
    }

    @Override
    @Transactional
    public void unsave(Long userId, Long adId) {
        if (!savedAdRepository.existsByUserIdAndAdId(userId, adId)) {
            throw new RuntimeException("Oglas nije u listi sačuvanih.");
        }
        savedAdRepository.deleteByUserIdAndAdId(userId, adId);
    }

    @Override
    public List<AdPreviewDto> getSavedAds(Long userId) {
        return savedAdRepository.findByUserIdOrderBySavedAtDesc(userId)
                .stream()
                .map(sa -> adMapper.toPreviewDto(sa.getAd()))
                .toList();
    }

    @Override
    public boolean isSaved(Long userId, Long adId) {
        return savedAdRepository.existsByUserIdAndAdId(userId, adId);
    }
}
