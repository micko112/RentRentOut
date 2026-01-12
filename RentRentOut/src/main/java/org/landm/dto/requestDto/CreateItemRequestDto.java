package org.landm.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.math.BigDecimal;

public class CreateItemRequestDto {
    @NotBlank
    private String name;

    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Positive
    private int days;
    
    @NotNull
    public long categoryId;

    @NotNull
    public LocationRequestDto location;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getDays() {
        return days;
    }
    public void setDays(int days) {
        this.days = days;
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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public LocationRequestDto getLocation() {
        return location;
    }

    public void setLocation(LocationRequestDto location) {
        this.location = location;
    }
}
