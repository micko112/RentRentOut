package org.landm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_report")
public class AdReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 60)
    private String reason;

    @Column(length = 500)
    private String note;

    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdReport() {}

    public AdReport(Ad ad, User reporter, String reason, String note) {
        this.ad = ad;
        this.reporter = reporter;
        this.reason = reason;
        this.note = note;
    }

    public Long getId() { return id; }
    public Ad getAd() { return ad; }
    public User getReporter() { return reporter; }
    public String getReason() { return reason; }
    public String getNote() { return note; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
