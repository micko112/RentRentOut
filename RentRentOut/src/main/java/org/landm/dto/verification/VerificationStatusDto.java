package org.landm.dto.verification;

import java.time.LocalDateTime;

public class VerificationStatusDto {
    private String status; // NONE | PENDING | APPROVED | REJECTED
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;

    public VerificationStatusDto() {}

    public VerificationStatusDto(String status,
                                 LocalDateTime submittedAt,
                                 LocalDateTime reviewedAt,
                                 String rejectionReason) {
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime v) { this.reviewedAt = v; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String v) { this.rejectionReason = v; }
}
