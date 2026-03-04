package org.landm.repository;

import org.landm.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
boolean existsByContractIdAndReviewerId(Long contractId, Long reviewerId);

}
