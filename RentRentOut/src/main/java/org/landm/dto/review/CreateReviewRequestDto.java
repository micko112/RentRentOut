package org.landm.dto.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.landm.entity.Enums.ReviewOption;
import org.landm.entity.Enums.ReviewType;
import org.landm.entity.RentalContract;
import org.landm.entity.User;

import java.time.LocalDateTime;

public class CreateReviewRequestDto {

    @NotNull
    private long contractId;

    @NotNull
    private ReviewOption paymentOk;

    @NotNull
    private ReviewOption communicationOk;

    @NotNull
    private ReviewOption agreementOk;

    private String comment;

    public long getContractId() {
        return contractId;
    }
    public void setContractId(long contractId) {
        this.contractId = contractId;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
