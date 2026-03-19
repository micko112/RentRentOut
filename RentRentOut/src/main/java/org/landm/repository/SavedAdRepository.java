package org.landm.repository;

import org.landm.entity.SavedAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedAdRepository extends JpaRepository<SavedAd, Long> {

    List<SavedAd> findByUserIdOrderBySavedAtDesc(Long userId);

    boolean existsByUserIdAndAdId(Long userId, Long adId);

    @Modifying
    @Query("DELETE FROM SavedAd sa WHERE sa.user.id = :userId AND sa.ad.id = :adId")
    void deleteByUserIdAndAdId(@Param("userId") Long userId, @Param("adId") Long adId);
}
