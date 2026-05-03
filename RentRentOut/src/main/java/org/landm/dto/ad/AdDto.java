package org.landm.dto.ad;

import org.landm.dto.CategoryDto;
import org.landm.dto.LocationDto;
import org.landm.dto.user.UserShortDto;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.landm.helper.DateInterval;

import java.math.BigDecimal;
import java.util.List;

public class AdDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    private BigDecimal deposit;

    private String currency;
    private PriceInterval priceInterval;
    private AdStatus adStatus;
    private int totalQuantity;

    private List<String> images;
    private UserShortDto owner;
    private CategoryDto category;
    private LocationDto location;

    private List<DateInterval> blockedIntervals;
    private int viewCount;
    private int saveCount;
    private boolean saved;

    // Detalji nekretnine
    private String advertiserType;
    private String roomCount;
    private java.math.BigDecimal areaSize;
    private String constructionType;
    private String propertyCondition;
    private String totalFloors;
    private String floorNumber;
    private String furnished;
    private List<String> heatingTypes;
    private String propertyMunicipality;
    private String propertyNeighborhood;
    private String propertyStreet;
    private java.math.BigDecimal landArea;
    private String landAreaUnit;
    private List<String> features;

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
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }
    public UserShortDto getOwner() {
        return owner;
    }
    public void setOwner(UserShortDto owner) {
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
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
	public List<DateInterval> getBlockedIntervals() {
		return blockedIntervals;
	}
	public void setBlockedIntervals(List<DateInterval> blockedIntervals) {
		this.blockedIntervals = blockedIntervals;
	}

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
    public void setPropertyMunicipality(String m) { this.propertyMunicipality = m; }

    public String getPropertyNeighborhood() { return propertyNeighborhood; }
    public void setPropertyNeighborhood(String n) { this.propertyNeighborhood = n; }

    public String getPropertyStreet() { return propertyStreet; }
    public void setPropertyStreet(String s) { this.propertyStreet = s; }

    public java.math.BigDecimal getLandArea() { return landArea; }
    public void setLandArea(java.math.BigDecimal landArea) { this.landArea = landArea; }

    public String getLandAreaUnit() { return landAreaUnit; }
    public void setLandAreaUnit(String landAreaUnit) { this.landAreaUnit = landAreaUnit; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    // Detalji automobila
    private String carBrand;
    private String carModel;
    private Integer carYear;
    private Integer carMileage;
    private String carBodyType;
    private String carFuelType;
    private String carTransmission;
    private Integer carPowerKw;
    private String carColor;
    private String carDoors;
    private Integer carSeats;
    private Integer carDisplacement;
    private String carEmissionClass;
    private String carDrive;
    private String carSteeringWheel;
    private String carRegisteredUntil;
    private String carCountry;
    private String carOrigin;
    private String carOwnership;
    private String carDamage;
    private String carLabel;
    private String carInteriorMaterial;
    private String carInteriorColor;

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
}
