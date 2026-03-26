package org.landm.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.landm.entity.Enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Aktivna promocija na oglasu — vraća se zajedno sa oglasom */
public class ActivePromotionDto {
    private Long id;
    private PromotionType promotionType;
    private String displayName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startsAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    private BigDecimal pricePaid;

    public ActivePromotionDto(Long id, PromotionType promotionType,
                              LocalDateTime startsAt, LocalDateTime expiresAt, BigDecimal pricePaid) {
        this.id = id;
        this.promotionType = promotionType;
        this.displayName = promotionType.getDisplayName();
        this.startsAt = startsAt;
        this.expiresAt = expiresAt;
        this.pricePaid = pricePaid;
    }

    public Long getId() { return id; }
    public PromotionType getPromotionType() { return promotionType; }
    public String getDisplayName() { return displayName; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public BigDecimal getPricePaid() { return pricePaid; }
}
