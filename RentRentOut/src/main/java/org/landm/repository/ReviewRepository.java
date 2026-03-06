package org.landm.repository;

import org.landm.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByContractIdAndReviewerId(Long contractId, Long reviewerId);

    Page<Review> findAllByRevieweeId(long revieweeId, Pageable pageable);

}
