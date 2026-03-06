package org.landm.mapper;

import org.landm.dto.review.ReviewDto;
import org.landm.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review){
        ReviewDto r = new ReviewDto();
        r.setId(review.getId());
        r.setContractId(review.getContract().getId());
        r.setReviewerId(review.getReviewer().getId());
        r.setRevieweeId(review.getReviewee().getId());
        r.setPaymentOk(review.getPaymentOk());
        r.setCommunicationOk(review.getCommunicationOk());
        r.setAgreementOk(review.getAgreementOk());
        r.setReviewType(review.getReviewType());
        r.setComment(review.getComment());
        r.setAdTitle(review.getContract().getAd().getTitle());
        r.setCreatedAT(review.getCreatedAT());
        return r;
    }
}
