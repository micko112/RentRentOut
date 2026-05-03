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

    @Column(name = "deposit")
    private BigDecimal deposit;

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

    // ── Detalji nekretnine (null za ostale kategorije) ───────────────────
    @Column(name = "advertiser_type")
    private String advertiserType;

    @Column(name = "room_count")
    private String roomCount;

    @Column(name = "area_size")
    private java.math.BigDecimal areaSize;

    @Column(name = "construction_type")
    private String constructionType;

    @Column(name = "property_condition")
    private String propertyCondition;

    @Column(name = "total_floors")
    private String totalFloors;

    @Column(name = "floor_number")
    private String floorNumber;

    @Column(name = "furnished")
    private String furnished;

    @ElementCollection
    @CollectionTable(name = "ad_heating_type", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name = "heating_type")
    private List<String> heatingTypes = new ArrayList<>();

    @Column(name = "property_municipality")
    private String propertyMunicipality;

    @Column(name = "property_neighborhood")
    private String propertyNeighborhood;

    @Column(name = "property_street")
    private String propertyStreet;

    @Column(name = "land_area")
    private java.math.BigDecimal landArea;

    @Column(name = "land_area_unit")
    private String landAreaUnit;

    @ElementCollection
    @CollectionTable(name = "ad_features", joinColumns = @JoinColumn(name = "ad_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    // ── Detalji automobila (null za ostale kategorije) ────────────────────
    @Column(name = "car_brand")            private String carBrand;
    @Column(name = "car_model")            private String carModel;
    @Column(name = "car_year")             private Integer carYear;
    @Column(name = "car_mileage")          private Integer carMileage;
    @Column(name = "car_body_type")        private String carBodyType;
    @Column(name = "car_fuel_type")        private String carFuelType;
    @Column(name = "car_transmission")     private String carTransmission;
    @Column(name = "car_power_kw")         private Integer carPowerKw;
    @Column(name = "car_color")            private String carColor;
    @Column(name = "car_doors")            private String carDoors;
    @Column(name = "car_seats")            private Integer carSeats;
    @Column(name = "car_displacement")     private Integer carDisplacement;
    @Column(name = "car_emission_class")   private String carEmissionClass;
    @Column(name = "car_drive")            private String carDrive;
    @Column(name = "car_steering_wheel")   private String carSteeringWheel;
    @Column(name = "car_registered_until") private String carRegisteredUntil;
    @Column(name = "car_country")          private String carCountry;
    @Column(name = "car_origin")           private String carOrigin;
    @Column(name = "car_ownership")        private String carOwnership;
    @Column(name = "car_damage")           private String carDamage;
    @Column(name = "car_label")            private String carLabel;
    @Column(name = "car_interior_material") private String carInteriorMaterial;
    @Column(name = "car_interior_color")   private String carInteriorColor;

    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String v) { this.carBrand = v; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String v) { this.carModel = v; }
    public Integer getCarYear() { return carYear; }
    public void setCarYear(Integer v) { this.carYear = v; }
    public Integer getCarMileage() { return carMileage; }
    public void setCarMileage(Integer v) { this.carMileage = v; }
    public String getCarBodyType() { return carBodyType; }
    public void setCarBodyType(String v) { this.carBodyType = v; }
    public String getCarFuelType() { return carFuelType; }
    public void setCarFuelType(String v) { this.carFuelType = v; }
    public String getCarTransmission() { return carTransmission; }
    public void setCarTransmission(String v) { this.carTransmission = v; }
    public Integer getCarPowerKw() { return carPowerKw; }
    public void setCarPowerKw(Integer v) { this.carPowerKw = v; }
    public String getCarColor() { return carColor; }
    public void setCarColor(String v) { this.carColor = v; }
    public String getCarDoors() { return carDoors; }
    public void setCarDoors(String v) { this.carDoors = v; }
    public Integer getCarSeats() { return carSeats; }
    public void setCarSeats(Integer v) { this.carSeats = v; }
    public Integer getCarDisplacement() { return carDisplacement; }
    public void setCarDisplacement(Integer v) { this.carDisplacement = v; }
    public String getCarEmissionClass() { return carEmissionClass; }
    public void setCarEmissionClass(String v) { this.carEmissionClass = v; }
    public String getCarDrive() { return carDrive; }
    public void setCarDrive(String v) { this.carDrive = v; }
    public String getCarSteeringWheel() { return carSteeringWheel; }
    public void setCarSteeringWheel(String v) { this.carSteeringWheel = v; }
    public String getCarRegisteredUntil() { return carRegisteredUntil; }
    public void setCarRegisteredUntil(String v) { this.carRegisteredUntil = v; }
    public String getCarCountry() { return carCountry; }
    public void setCarCountry(String v) { this.carCountry = v; }
    public String getCarOrigin() { return carOrigin; }
    public void setCarOrigin(String v) { this.carOrigin = v; }
    public String getCarOwnership() { return carOwnership; }
    public void setCarOwnership(String v) { this.carOwnership = v; }
    public String getCarDamage() { return carDamage; }
    public void setCarDamage(String v) { this.carDamage = v; }
    public String getCarLabel() { return carLabel; }
    public void setCarLabel(String v) { this.carLabel = v; }
    public String getCarInteriorMaterial() { return carInteriorMaterial; }
    public void setCarInteriorMaterial(String v) { this.carInteriorMaterial = v; }
    public String getCarInteriorColor() { return carInteriorColor; }
    public void setCarInteriorColor(String v) { this.carInteriorColor = v; }

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

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
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

    public String getAdvertiserType() { return advertiserType; }
    public void setAdvertiserType(String advertiserType) { this.advertiserType = advertiserType; }

    public String getRoomCount() { return roomCount; }
    public void setRoomCount(String roomCount) { this.roomCount = roomCount; }

    public java.math.BigDecimal getAreaSize() { return areaSize; }
    public void setAreaSize(java.math.BigDecimal areaSize) { this.areaSize = areaSize; }

    public String getConstructionType() { return constructionType; }
    public void setConstructionType(String constructionType) { this.constructionType = constructionType; }

    public String getPropertyCondition() { return propertyCondition; }
    public void setPropertyCondition(String propertyCondition) { this.propertyCondition = propertyCondition; }

    public String getTotalFloors() { return totalFloors; }
    public void setTotalFloors(String totalFloors) { this.totalFloors = totalFloors; }

    public String getFloorNumber() { return floorNumber; }
    public void setFloorNumber(String floorNumber) { this.floorNumber = floorNumber; }

    public String getFurnished() { return furnished; }
    public void setFurnished(String furnished) { this.furnished = furnished; }

    public List<String> getHeatingTypes() { return heatingTypes; }
    public void setHeatingTypes(List<String> heatingTypes) { this.heatingTypes = heatingTypes; }

    public String getPropertyMunicipality() { return propertyMunicipality; }
    public void setPropertyMunicipality(String propertyMunicipality) { this.propertyMunicipality = propertyMunicipality; }

    public String getPropertyNeighborhood() { return propertyNeighborhood; }
    public void setPropertyNeighborhood(String propertyNeighborhood) { this.propertyNeighborhood = propertyNeighborhood; }

    public String getPropertyStreet() { return propertyStreet; }
    public void setPropertyStreet(String propertyStreet) { this.propertyStreet = propertyStreet; }

    public java.math.BigDecimal getLandArea() { return landArea; }
    public void setLandArea(java.math.BigDecimal landArea) { this.landArea = landArea; }

    public String getLandAreaUnit() { return landAreaUnit; }
    public void setLandAreaUnit(String landAreaUnit) { this.landAreaUnit = landAreaUnit; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

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
