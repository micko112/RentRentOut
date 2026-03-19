package org.landm.repository;

import org.landm.entity.AdView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdViewRepository extends JpaRepository<AdView, Long> {

    boolean existsByUserIdAndAdId(Long userId, Long adId);

    long countByAdId(Long adId);
}
