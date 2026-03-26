package org.landm.entity.Enums;

public enum PromotionType {

    /**
     * Na vrhu — uvek prvi u pretrazi i kategoriji. Rank 3.
     * Cena: 500 RSD / 7 dana.
     */
    FEATURED(3, 500, 7, "Na vrhu"),

    /**
     * Prioritetni — ispred standardnih, iza FEATURED. Rank 2.
     * Cena: 250 RSD / 3 dana.
     */
    PRIORITY(2, 250, 3, "Prioritetni"),

    /**
     * Istaknut oglas — samo vizuelno isticanje (boja kartice). Rank 0 (ne utiče na poziciju).
     * Cena: 100 RSD / 30 dana.
     */
    HIGHLIGHTED(0, 100, 30, "Istaknut oglas");

    private final int rank;
    private final int priceRsd;
    private final int durationDays;
    private final String displayName;

    PromotionType(int rank, int priceRsd, int durationDays, String displayName) {
        this.rank = rank;
        this.priceRsd = priceRsd;
        this.durationDays = durationDays;
        this.displayName = displayName;
    }

    public int getRank() { return rank; }
    public int getPriceRsd() { return priceRsd; }
    public int getDurationDays() { return durationDays; }
    public String getDisplayName() { return displayName; }
}
