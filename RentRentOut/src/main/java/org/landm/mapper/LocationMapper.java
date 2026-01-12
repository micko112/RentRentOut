package org.landm.mapper;

import org.landm.dto.LocationDto;
import org.landm.dto.requestDto.LocationRequestDto;
import org.landm.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public LocationDto toDto(Location l) {
        LocationDto dto = new LocationDto();
        dto.setId(l.getId());
        dto.setCountry(l.getCountry());
        dto.setCity(l.getCity());
        dto.setMunicipality(l.getMunicipality());
        dto.setLat(l.getLat());
        dto.setLng(l.getLng());
        return dto;
    }
    public Location toEntity(LocationRequestDto dto) {
        Location l = new Location();
        l.setCountry(dto.getCountry());
        l.setCity(dto.getCity());
        l.setMunicipality(dto.getMunicipality());
        l.setLat(dto.getLat());
        l.setLng(dto.getLng());
        return l;
    }
}
