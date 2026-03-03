package org.landm.dto.ad;

import org.landm.dto.LocationDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Location;

import java.math.BigDecimal;

public class AdPreviewDto {

    private long id;
    private String title;
    private String city;
    private String municipality;
    private String description;
    private BigDecimal price;
    private String currency;
    private PriceInterval priceInterval;
    private String thumbnail;
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
