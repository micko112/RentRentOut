package org.landm.repository;

import org.landm.entity.SavedAd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SavedAdRepository extends JpaRepository<SavedAd, Long> {

    Optional<SavedAd> findByUserIdAndAdId(Long userId, Long adId);

    boolean existsByUserIdAndAdId(Long userId, Long adId);

    @Query("SELECT s.ad.id FROM SavedAd s WHERE s.user.id = :userId AND s.ad.id IN :adIds")
    Set<Long> findSavedAdIdsByUserIdAndAdIdIn(@Param("userId") Long userId, @Param("adIds") List<Long> adIds);

    Page<SavedAd> findAllByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SavedAd s WHERE s.user.id = :userId AND s.ad.id = :adId")
    void deleteByUserIdAndAdId(Long userId, Long adId);
}
