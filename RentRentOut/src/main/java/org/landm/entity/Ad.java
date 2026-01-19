package org.landm.entity;


import jakarta.persistence.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.DeleteStatus;
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
    private long id;
    @Column(nullable = false, name="title")
    private String title;
    @Column(name="description")
    private String description;

    @Column(nullable = false, name="price")
    private BigDecimal price;
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
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity = 1;
//    @PrePersist
//    public void onPrePersist() {
//        this.availableQuantity = this.totalQuantity;
//    }
    @ElementCollection
    @CollectionTable(name = "ad_image", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name="image_url")
    private List<String> images = new ArrayList<>();

    public Ad(String title, String description, BigDecimal price, PriceInterval priceInterval, User owner, Category category,
              Location location, AdStatus adStatus, int totalQuantity, int availableQuantity, List<String> images) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.priceInterval = priceInterval;
        this.owner = owner;
        this.category = category;
        this.location = location;
        this.adStatus = adStatus;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.images = images;
    }
    public Ad(String title, String description, BigDecimal price, PriceInterval priceInterval, User owner, Category category,
              Location location, AdStatus adStatus, int totalQuantity, List<String> images) {
        this.title = title;
        this.description = description;
        this.price = price;
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
                ", available_quantity=" + availableQuantity +
                ", images=" + images +
                '}';
    }
}
