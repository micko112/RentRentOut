package org.landm.dto.ad;

import org.landm.dto.CategoryDto;
import org.landm.dto.LocationDto;
import org.landm.dto.user.UserDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.landm.helper.DateInterval;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    // email znaci username
    private String email;
    private UserDto owner;
    private CategoryDto category;
    private LocationDto location;
    
    private List<DateInterval> blockedIntervals;

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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<DateInterval> getBlockedIntervals() {
		return blockedIntervals;
	}
	public void setBlockedIntervals(List<DateInterval> blockedIntervals) {
		this.blockedIntervals = blockedIntervals;
	}    
    
}
