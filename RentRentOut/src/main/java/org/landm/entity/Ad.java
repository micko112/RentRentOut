package org.landm.entity;


import jakarta.persistence.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "ad")
public class Ad {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name="title")
    private String title;
    @Column(name="description")
    private String description;

    @Column(nullable = false, name="price")
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, name="currency")
    private Currency currency = Currency.RSD;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="price_interval")
    private PriceInterval priceInterval;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

//    treba dodati nullable = false kad svaki Ad ima lokaciju!!!!
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "ad_status")
    private AdStatus adStatus;
    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity = 1; // Podrazumevano 1 za unikatne stvari


    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @ElementCollection
    @CollectionTable(name = "ad_image", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name="image_url")
    private List<String> images = new ArrayList<>();

    public Ad(String title, String description, BigDecimal price, Currency currency, PriceInterval priceInterval, User owner, Category category,
              Location location, AdStatus adStatus, int totalQuantity,  List<String> images) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.priceInterval = priceInterval;
        this.owner = owner;
        this.category = category;
        this.location = location;
        this.adStatus = adStatus;
        this.totalQuantity = totalQuantity;
        this.images = images;
    }


    public Ad() {

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
    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
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

    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

	@Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return id == ad.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Ad{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", priceInterval=" + priceInterval +
                ", owner=" + owner +
                ", category=" + category +
                ", location=" + location +
                ", adStatus=" + adStatus +
                ", total_quantity=" + totalQuantity +
                ", images=" + images +
                '}';
    }
}
