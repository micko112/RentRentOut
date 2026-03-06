package org.landm.entity;

import jakarta.persistence.*;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;

import java.time.LocalDateTime;

@Entity
@Table(name="review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private RentalContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_ok", nullable = false)
    private ReviewOption paymentOk;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_ok", nullable = false)
    private ReviewOption communicationOk;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_ok", nullable = false)
    private ReviewOption agreementOk;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Review(RentalContract contract, User reviewer, User reviewee, ReviewOption paymentOk, ReviewOption communicationOk, ReviewOption agreementOk, ReviewType reviewType, String comment, LocalDateTime createdAt) {

        this.contract = contract;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.paymentOk = paymentOk;
        this.communicationOk = communicationOk;
        this.agreementOk = agreementOk;
        this.reviewType = reviewType;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review() {

    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public RentalContract getContract() {
        return contract;
    }

    public void setContract(RentalContract contract) {
        this.contract = contract;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public User getReviewee() {
        return reviewee;
    }

    public void setReviewee(User reviewee) {
        this.reviewee = reviewee;
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
}
