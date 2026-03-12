package org.landm.dto.review;


import org.landm.dto.user.UserShortDto;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;

import java.time.LocalDateTime;

public class ReviewDto {

    private Long id;

    private Long contractId;

    private UserShortDto reviewer;

    private UserShortDto reviewee;

    private String adTitle;

    private ReviewOption paymentOk;

    private ReviewOption communicationOk;

    private ReviewOption agreementOk;

    private ReviewType reviewType;

    private String comment;

    private LocalDateTime createdAt;

    private String revieweeRole;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public UserShortDto getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserShortDto reviewer) {
        this.reviewer = reviewer;
    }

    public UserShortDto getReviewee() {
        return reviewee;
    }

    public void setReviewee(UserShortDto reviewee) {
        this.reviewee = reviewee;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public ReviewOption getPaymentOk() {
        return paymentOk;
    }

    public void setPaymentOk(ReviewOption paymentOk) {
        this.paymentOk = paymentOk;
    }

    public ReviewOption getCommunicationOk() {
        return communicationOk;
    }

    public void setCommunicationOk(ReviewOption communicationOk) {
        this.communicationOk = communicationOk;
    }

    public ReviewOption getAgreementOk() {
        return agreementOk;
    }

    public void setAgreementOk(ReviewOption agreementOk) {
        this.agreementOk = agreementOk;
    }

    public String getRevieweeRole() {
        return revieweeRole;
    }

    public void setRevieweeRole(String revieweeRole) {
        this.revieweeRole = revieweeRole;
    }
}
