package org.landm.service;

import org.landm.dto.review.CreateReviewRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.landm.entity.Review;

public interface ReviewService  {
    public ReviewType calculateReviewType(ReviewOption payment, ReviewOption communication, ReviewOption agreement);

    public ReviewDto createReview(CreateReviewRequestDto dto, long reviewerId);

}
