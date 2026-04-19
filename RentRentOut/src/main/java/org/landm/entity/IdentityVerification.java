package org.landm.entity;

import jakarta.persistence.*;
import org.landm.entity.Enums.VerificationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "identity_verification")
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "doc_front_public_id", length = 255)
    private String docFrontPublicId;

    @Column(name = "doc_back_public_id", length = 255)
    private String docBackPublicId;

    @Column(name = "selfie_public_id", length = 255)
    private String selfiePublicId;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    public IdentityVerification() {}

    public IdentityVerification(User user,
                                String docFrontPublicId,
                                String docBackPublicId,
                                String selfiePublicId) {
        this.user = user;
        this.docFrontPublicId = docFrontPublicId;
        this.docBackPublicId = docBackPublicId;
        this.selfiePublicId = selfiePublicId;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }

    public String getDocFrontPublicId() { return docFrontPublicId; }
    public void setDocFrontPublicId(String v) { this.docFrontPublicId = v; }

    public String getDocBackPublicId() { return docBackPublicId; }
    public void setDocBackPublicId(String v) { this.docBackPublicId = v; }

    public String getSelfiePublicId() { return selfiePublicId; }
    public void setSelfiePublicId(String v) { this.selfiePublicId = v; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime v) { this.reviewedAt = v; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String v) { this.rejectionReason = v; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User v) { this.reviewedBy = v; }
}
