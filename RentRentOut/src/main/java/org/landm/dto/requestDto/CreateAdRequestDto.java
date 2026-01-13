package org.landm.dto.requestDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.landm.dto.CategoryDto;
import org.landm.dto.LocationDto;
import org.landm.dto.UserDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;

import java.math.BigDecimal;
import java.util.List;

public class CreateAdRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    @Positive
    private BigDecimal price;
    @NotNull
    private PriceInterval priceInterval;
    @NotNull
    private long categoryId;
    @NotNull
    private long locationId;
    @Min(1)
    private int totalQuantity = 1;

    // ovde moze not null posle
    private List<String> images;

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

    public PriceInterval getPriceInterval() {
        return priceInterval;
    }

    public void setPriceInterval(PriceInterval priceInterval) {
        this.priceInterval = priceInterval;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
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
}
