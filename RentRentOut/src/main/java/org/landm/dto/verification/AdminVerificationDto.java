package org.landm.dto.verification;

import java.time.LocalDateTime;

/**
 * DTO za admin listu verifikacija (bez signed URL-ova).
 */
public class AdminVerificationDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;

    public AdminVerificationDto() {}

    public AdminVerificationDto(Long id, Long userId, String userFullName, String userEmail,
                                String status, LocalDateTime submittedAt,
                                LocalDateTime reviewedAt, String rejectionReason) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String v) { this.userFullName = v; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String v) { this.userEmail = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime v) { this.reviewedAt = v; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String v) { this.rejectionReason = v; }
}
