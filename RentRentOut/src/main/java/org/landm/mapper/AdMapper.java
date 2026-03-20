package org.landm.mapper;



import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.CreateAdRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.springframework.stereotype.Component;

@Component
public class AdMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;

    // Injektujemo druge mappere koji su nam potrebni
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
        dto.setPriceInterval(PriceInterval.valueOf(ad.getPriceInterval().name()));
        dto.setCurrency(ad.getCurrency().toString());
        dto.setAdStatus(AdStatus.valueOf(ad.getAdStatus().name()));
        dto.setTotalQuantity(ad.getTotalQuantity());
        dto.setImages(ad.getImages());

        dto.setOwner(userMapper.toUserShortDto(ad.getOwner()));
        dto.setCategory(categoryMapper.toDto(ad.getCategory()));
        dto.setLocation((locationMapper.toDto(ad.getLocation())));
        dto.setViewCount(ad.getViewCount());
        dto.setSaveCount(ad.getSaveCount());

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
        dto.setPriceInterval(PriceInterval.valueOf(ad.getPriceInterval().name()));

        if (ad.getImages() != null && !ad.getImages().isEmpty()) {
            dto.setThumbnail(ad.getImages().get(0));
        }
        dto.setCity(ad.getLocation().getCity());
        dto.setMunicipality(ad.getLocation().getMunicipality());
        dto.setAdStatus(ad.getAdStatus());
        dto.setViewCount(ad.getViewCount());
        dto.setSaveCount(ad.getSaveCount());
        dto.setCreatedAt(ad.getCreatedAt());

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
