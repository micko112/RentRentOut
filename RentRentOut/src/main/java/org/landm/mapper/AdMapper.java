package org.landm.mapper;



import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.CreateAdRequestDto;
import org.landm.entity.Ad;
import org.springframework.stereotype.Component;

@Component
public class AdMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;

    public AdMapper(UserMapper userMapper, CategoryMapper categoryMapper, LocationMapper locationMapper) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.locationMapper = locationMapper;
    }
    public AdDto toDto(Ad ad){
        AdDto dto = new AdDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setPricePerWeek(ad.getPricePerWeek());
        dto.setPricePerMonth(ad.getPricePerMonth());
        dto.setDeposit(ad.getDeposit());
        dto.setPriceInterval(ad.getPriceInterval());
        dto.setCurrency(ad.getCurrency().toString());
        dto.setAdStatus(ad.getAdStatus());
        dto.setTotalQuantity(ad.getTotalQuantity());
        dto.setImages(ad.getImages());

        dto.setOwner(userMapper.toUserShortDto(ad.getOwner()));
        dto.setCategory(categoryMapper.toDto(ad.getCategory()));
        dto.setLocation((locationMapper.toDto(ad.getLocation())));
        dto.setViewCount(ad.getViewCount());
        dto.setSaveCount(ad.getSaveCount());

        dto.setAdvertiserType(ad.getAdvertiserType());
        dto.setRoomCount(ad.getRoomCount());
        dto.setAreaSize(ad.getAreaSize());
        dto.setConstructionType(ad.getConstructionType());
        dto.setPropertyCondition(ad.getPropertyCondition());
        dto.setTotalFloors(ad.getTotalFloors());
        dto.setFloorNumber(ad.getFloorNumber());
        dto.setFurnished(ad.getFurnished());
        dto.setHeatingTypes(ad.getHeatingTypes());
        dto.setPropertyMunicipality(ad.getPropertyMunicipality());
        dto.setPropertyNeighborhood(ad.getPropertyNeighborhood());
        dto.setPropertyStreet(ad.getPropertyStreet());
        dto.setLandArea(ad.getLandArea());
        dto.setLandAreaUnit(ad.getLandAreaUnit());
        dto.setFeatures(ad.getFeatures());
        dto.setCarBrand(ad.getCarBrand());
        dto.setCarModel(ad.getCarModel());
        dto.setCarYear(ad.getCarYear());
        dto.setCarMileage(ad.getCarMileage());
        dto.setCarBodyType(ad.getCarBodyType());
        dto.setCarFuelType(ad.getCarFuelType());
        dto.setCarTransmission(ad.getCarTransmission());
        dto.setCarPowerKw(ad.getCarPowerKw());
        dto.setCarColor(ad.getCarColor());
        dto.setCarDoors(ad.getCarDoors());
        dto.setCarSeats(ad.getCarSeats());
        dto.setCarDisplacement(ad.getCarDisplacement());
        dto.setCarEmissionClass(ad.getCarEmissionClass());
        dto.setCarDrive(ad.getCarDrive());
        dto.setCarSteeringWheel(ad.getCarSteeringWheel());
        dto.setCarRegisteredUntil(ad.getCarRegisteredUntil());
        dto.setCarCountry(ad.getCarCountry());
        dto.setCarOrigin(ad.getCarOrigin());
        dto.setCarOwnership(ad.getCarOwnership());
        dto.setCarDamage(ad.getCarDamage());
        dto.setCarLabel(ad.getCarLabel());
        dto.setCarInteriorMaterial(ad.getCarInteriorMaterial());
        dto.setCarInteriorColor(ad.getCarInteriorColor());

        return dto;
    }
    public AdPreviewDto toPreviewDto(Ad ad){
        AdPreviewDto dto = new AdPreviewDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setPricePerWeek(ad.getPricePerWeek());
        dto.setPricePerMonth(ad.getPricePerMonth());
        dto.setCurrency(ad.getCurrency().toString());
        dto.setPriceInterval(ad.getPriceInterval());

        if (ad.getImages() != null && !ad.getImages().isEmpty()) {
            dto.setThumbnail(ad.getImages().get(0));
        }
        if (ad.getLocation() != null) {
            dto.setCity(ad.getLocation().getCity());
            dto.setMunicipality(ad.getLocation().getMunicipality());
        }
        dto.setAdStatus(ad.getAdStatus());
        dto.setViewCount(ad.getViewCount());
        dto.setSaveCount(ad.getSaveCount());
        dto.setCreatedAt(ad.getCreatedAt());
        dto.setExpiresAt(ad.getExpiresAt());
        dto.setPromotionType(ad.getPromotionType());

        return dto;
    }


    public Ad toEntity(CreateAdRequestDto dto){
        if (dto == null) {
            return null;
        }
        Ad ad = new Ad();
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice());
        ad.setPriceInterval(dto.getPriceInterval());
        ad.setTotalQuantity(dto.getTotalQuantity());
        ad.setImages(dto.getImages());

        return ad;
    }

}
