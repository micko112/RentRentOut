package org.landm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.landm.entity.Category;


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

    @NotNull
    public Category category;

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
