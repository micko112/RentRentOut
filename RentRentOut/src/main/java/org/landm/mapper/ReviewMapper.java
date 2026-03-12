package org.landm.mapper;

import org.landm.dto.review.ReviewDto;
import org.landm.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    private final UserMapper userMapper;

    public ReviewMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ReviewDto toDto(Review review){
        ReviewDto r = new ReviewDto();
        r.setId(review.getId());
        r.setContractId(review.getContract().getId());
        r.setReviewer(userMapper.toUserShortDto(review.getReviewer()));
        r.setReviewee(userMapper.toUserShortDto(review.getReviewee()));
        r.setPaymentOk(review.getPaymentOk());
        r.setCommunicationOk(review.getCommunicationOk());
        r.setAgreementOk(review.getAgreementOk());
        r.setReviewType(review.getReviewType());
        r.setComment(review.getComment());
        r.setAdTitle(review.getContract().getAd().getTitle());
        r.setCreatedAt(review.getCreatedAt());

        if (review.getReviewee().getId() == review.getContract().getAd().getOwner().getId()) {
            r.setRevieweeRole("LESSOR");
        } else {
            r.setRevieweeRole("LESSEE");
        }

        return r;
    }
}
