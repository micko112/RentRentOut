package org.landm.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_ad",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_saved_ad_user_ad",
                columnNames = {"user_id", "ad_id"}
        ))
public class SavedAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    public SavedAd() {}

    public SavedAd(User user, Ad ad) {
        this.user = user;
        this.ad = ad;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ad getAd() { return ad; }
    public void setAd(Ad ad) { this.ad = ad; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
