package org.landm.dto.ad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.landm.dto.CategoryDto;
import org.landm.dto.LocationDto;
import org.landm.dto.user.UserDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;

import java.math.BigDecimal;

import java.util.List;

public class UpdateAdRequestDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private BigDecimal price;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    @NotNull
    private Currency currency;
    @NotNull
    private PriceInterval priceInterval;
    // ad status se automatski menja
    @NotNull
    private int totalQuantity;
    @NotEmpty
    private List<String> images;
    // email znaci username
    @NotNull
    private Long categoryId;
    @NotNull
    private Long locationId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPricePerWeek() {
        return pricePerWeek;
    }

    public void setPricePerWeek(BigDecimal pricePerWeek) {
        this.pricePerWeek = pricePerWeek;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(BigDecimal pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public PriceInterval getPriceInterval() {
        return priceInterval;
    }

    public void setPriceInterval(PriceInterval priceInterval) {
        this.priceInterval = priceInterval;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
