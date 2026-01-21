package org.landm.dto.ad;

import org.landm.entity.Enums.PriceInterval;

import java.math.BigDecimal;

public class AdSearchCriteriaDto {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private PriceInterval priceInterval;
    private Long locationId;

    public AdSearchCriteriaDto() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public PriceInterval getPriceInterval() {
        return priceInterval;
    }

    public void setPriceInterval(PriceInterval priceInterval) {
        this.priceInterval = priceInterval;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
