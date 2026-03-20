package org.landm.dto.ad;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.landm.dto.LocationDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Location;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdPreviewDto {

    private Long id;
    private String title;
    private String city;
    private String municipality;
    private String description;
    private BigDecimal price;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    private String currency;
    private PriceInterval priceInterval;
    private String thumbnail;
    private AdStatus adStatus;
    private int viewCount;
    private int saveCount;
    private boolean saved;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public AdStatus getAdStatus() {
        return adStatus;
    }

    public void setAdStatus(AdStatus adStatus) {
        this.adStatus = adStatus;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PriceInterval getPriceInterval() {
        return priceInterval;
    }

    public void setPriceInterval(PriceInterval priceInterval) {
        this.priceInterval = priceInterval;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(int saveCount) {
        this.saveCount = saveCount;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
