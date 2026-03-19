package org.landm.repository;

import org.landm.entity.SavedAd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedAdRepository extends JpaRepository<SavedAd, Long> {

    Optional<SavedAd> findByUserIdAndAdId(Long userId, Long adId);

    boolean existsByUserIdAndAdId(Long userId, Long adId);

    Page<SavedAd> findAllByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SavedAd s WHERE s.user.id = :userId AND s.ad.id = :adId")
    void deleteByUserIdAndAdId(Long userId, Long adId);
}
