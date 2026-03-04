package org.landm.dto.review;


import jakarta.validation.constraints.NotNull;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;

import java.time.LocalDateTime;

public class ReviewDto {

    private long id;

    private long contractId;

    private long reviewerId;

    private long revieweeId;

    private String adTitle;

    private ReviewType reviewType;

    private String comment;

    private LocalDateTime createdAT;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContractId() {
        return contractId;
    }

    public void setContractId(long contractId) {
        this.contractId = contractId;
    }


    public long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public ReviewType getReviewType() {
        return reviewType;
    }

    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAT() {
        return createdAT;
    }

    public void setCreatedAT(LocalDateTime createdAT) {
        this.createdAT = createdAT;
    }

    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }
}
