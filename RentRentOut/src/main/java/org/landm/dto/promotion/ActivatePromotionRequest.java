package org.landm.dto.promotion;

import jakarta.validation.constraints.NotNull;
import org.landm.entity.Enums.PromotionType;

public class ActivatePromotionRequest {

    @NotNull(message = "ID oglasa je obavezan.")
    private Long adId;

    @NotNull(message = "Tip promocije je obavezan.")
    private PromotionType promotionType;

    public Long getAdId() { return adId; }
    public void setAdId(Long adId) { this.adId = adId; }

    public PromotionType getPromotionType() { return promotionType; }
    public void setPromotionType(PromotionType promotionType) { this.promotionType = promotionType; }
}
