package org.landm.mapper;



import org.landm.dto.AdDto;
import org.landm.dto.CategoryDto;
import org.landm.dto.LocationDto;
import org.landm.dto.UserDto;
import org.landm.dto.requestDto.CreateAdRequestDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Location;
import org.landm.entity.User;
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
        dto.setPriceInterval(PriceInterval.valueOf(ad.getPriceInterval().name()));
        dto.setAdStatus(AdStatus.valueOf(ad.getAdStatus().name()));
        dto.setTotalQuantity(ad.getTotalQuantity());
        dto.setAvailableQuantity(ad.getAvailableQuantity());
        dto.setImages(ad.getImages());

        dto.setOwner(userMapper.toDto(ad.getOwner()));
        dto.setCategory(categoryMapper.toDto(ad.getCategory()));
        dto.setLocation((locationMapper.toDto(ad.getLocation())));

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
