package org.landm.entity;


import jakarta.persistence.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "price_per_week")
    private BigDecimal pricePerWeek;

    @Column(name = "price_per_month")
    private BigDecimal pricePerMonth;

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

    @Column(name = "save_count", nullable = false)
    private int saveCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Oglas ističe 30 dana od kreiranja (besplatno obnavljanje) */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Aktivna promocija (denormalizovano za brzo sortiranje) */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionType promotionType;

    @Column(name = "promotion_expires_at")
    private LocalDateTime promotionExpiresAt;

    /**
     * 0=standard/HIGHLIGHTED, 2=PRIORITY, 3=FEATURED.
     * Sortira se DESC — viši rank ide prvi u pretrazi.
     */
    @Column(name = "promotion_rank", nullable = false)
    private int promotionRank = 0;

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

    public int getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(int saveCount) {
        this.saveCount = saveCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public PromotionType getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(PromotionType promotionType) {
        this.promotionType = promotionType;
    }

    public LocalDateTime getPromotionExpiresAt() {
        return promotionExpiresAt;
    }

    public void setPromotionExpiresAt(LocalDateTime promotionExpiresAt) {
        this.promotionExpiresAt = promotionExpiresAt;
    }

    public int getPromotionRank() {
        return promotionRank;
    }

    public void setPromotionRank(int promotionRank) {
        this.promotionRank = promotionRank;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusDays(30);
        }
    }

	@Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return Objects.equals(id, ad.id);
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
