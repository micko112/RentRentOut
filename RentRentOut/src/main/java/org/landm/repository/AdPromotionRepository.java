package org.landm.repository;

import org.landm.entity.AdPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdPromotionRepository extends JpaRepository<AdPromotion, Long> {

    List<AdPromotion> findAllByAdId(Long adId);

    /** Sve promocije koje su istekle a oglas još ima promotion_rank > 0 */
    @Query("""
        SELECT p FROM AdPromotion p
        WHERE p.expiresAt < :now
          AND p.ad.promotionRank > 0
    """)
    List<AdPromotion> findExpiredActivePromotions(LocalDateTime now);

    /** Trenutno aktivna promocija za oglas */
    @Query("""
        SELECT p FROM AdPromotion p
        WHERE p.ad.id = :adId
          AND p.expiresAt > :now
        ORDER BY p.expiresAt DESC
    """)
    List<AdPromotion> findActiveByAdId(Long adId, LocalDateTime now);
}
