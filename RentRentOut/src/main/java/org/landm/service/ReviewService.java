package org.landm.service;

import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.dto.review.ReviewEligibilityDto;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService  {
    public ReviewType calculateReviewType(ReviewOption payment, ReviewOption communication, ReviewOption agreement);

    public ReviewDto createReview(CreateReviewRequestDto dto, Long reviewerId);

    public ReviewEligibilityDto checkEligibility(Long contractId, Long reviewerId);

   public Page<ReviewDto> getAllForUser(Pageable pageable, Long revieweeId);

   public Long findContractWithUser(Long currentUserId, Long otherUserId);
}
