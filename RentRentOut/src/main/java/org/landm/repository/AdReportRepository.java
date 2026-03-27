package org.landm.repository;

import org.landm.entity.AdReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdReportRepository extends JpaRepository<AdReport, Long> {
    boolean existsByAdIdAndReporterId(Long adId, Long reporterId);
    Page<AdReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<AdReport> findAllByReviewedFalseOrderByCreatedAtDesc(Pageable pageable);
    long countByReviewedFalse();
}
