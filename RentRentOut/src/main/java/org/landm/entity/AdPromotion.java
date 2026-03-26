package org.landm.entity;

import jakarta.persistence.*;
import org.landm.entity.Enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_promotion")
public class AdPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false)
    private PromotionType promotionType;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "price_paid", nullable = false)
    private BigDecimal pricePaid;

    public AdPromotion() {}

    public AdPromotion(Ad ad, User user, PromotionType promotionType,
                       LocalDateTime startsAt, LocalDateTime expiresAt, BigDecimal pricePaid) {
        this.ad = ad;
        this.user = user;
        this.promotionType = promotionType;
        this.startsAt = startsAt;
        this.expiresAt = expiresAt;
        this.pricePaid = pricePaid;
    }

    public Long getId() { return id; }
    public Ad getAd() { return ad; }
    public User getUser() { return user; }
    public PromotionType getPromotionType() { return promotionType; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public BigDecimal getPricePaid() { return pricePaid; }
}
