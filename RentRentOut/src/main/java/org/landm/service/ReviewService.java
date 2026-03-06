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

    public ReviewDto createReview(CreateReviewRequestDto dto, long reviewerId);

    public ReviewEligibilityDto checkEligibility(long contractId, long reviewerId);

   public Page<ReviewDto> getAllForUser(Pageable pageable, long revieweeId);
}
