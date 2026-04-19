package org.landm.dto.verification;

import java.time.LocalDateTime;

/**
 * DTO sa signed URL-ovima slika za admin pregled.
 * URL-ovi su privremeni (važe 30 minuta).
 */
public class AdminVerificationDetailsDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String status;
    private LocalDateTime submittedAt;
    private String docFrontUrl;
    private String docBackUrl;
    private String selfieUrl;

    public AdminVerificationDetailsDto() {}

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

    public String getDocFrontUrl() { return docFrontUrl; }
    public void setDocFrontUrl(String v) { this.docFrontUrl = v; }

    public String getDocBackUrl() { return docBackUrl; }
    public void setDocBackUrl(String v) { this.docBackUrl = v; }

    public String getSelfieUrl() { return selfieUrl; }
    public void setSelfieUrl(String v) { this.selfieUrl = v; }
}
