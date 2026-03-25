package org.landm.dto.review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.landm.entity.Enums.ReviewOption;

public class CreateReviewRequestDto {

    @NotNull
    private Long contractId;

    @NotNull
    private ReviewOption paymentOk;

    @NotNull
    private ReviewOption communicationOk;

    @NotNull
    private ReviewOption agreementOk;

    @Size(max = 2000)
    private String comment;

    public Long getContractId() {
        return contractId;
    }
    public void setContractId(Long contractId) {
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
