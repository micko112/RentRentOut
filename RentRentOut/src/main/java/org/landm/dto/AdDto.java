package org.landm.dto;

import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;

import java.math.BigDecimal;
import java.util.List;

public class AdDto {
    private long id;
    private String title;
    private String description;
    private BigDecimal price;
    private PriceInterval priceInterval;
    private AdStatus adStatus;
    private int totalQuantity;
    private int availableQuantity;
    private List<String> images;

    private UserDto owner;
    private CategoryDto category;
    private LocationDto location;

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
    public AdStatus getAdStatus() {
        return adStatus;
    }
    public void setAdStatus(AdStatus adStatus) {
        this.adStatus = adStatus;
    }
    public int getTotalQuantity() {
        return totalQuantity;
    }
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    public int getAvailableQuantity() {
        return availableQuantity;
    }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }
    public UserDto getOwner() {
        return owner;
    }
    public void setOwner(UserDto owner) {
        this.owner = owner;
    }
    public CategoryDto getCategory() {
        return category;
    }
    public void setCategory(CategoryDto category) {
        this.category = category;
    }
    public LocationDto getLocation() {
        return location;
    }
    public void setLocation(LocationDto location) {
        this.location = location;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
}
