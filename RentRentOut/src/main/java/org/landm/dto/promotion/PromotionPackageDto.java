package org.landm.dto.promotion;

import org.landm.entity.Enums.PromotionType;

/** Paket promocije koji se prikazuje korisniku na stranici "Promoviši oglas" */
public class PromotionPackageDto {
    private PromotionType type;
    private String displayName;
    private int priceRsd;
    private int durationDays;
    private String description;

    public PromotionPackageDto(PromotionType type, String description) {
        this.type = type;
        this.displayName = type.getDisplayName();
        this.priceRsd = type.getPriceRsd();
        this.durationDays = type.getDurationDays();
        this.description = description;
    }

    public PromotionType getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getPriceRsd() { return priceRsd; }
    public int getDurationDays() { return durationDays; }
    public String getDescription() { return description; }
}
