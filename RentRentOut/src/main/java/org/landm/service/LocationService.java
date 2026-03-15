package org.landm.service;

import org.landm.dto.LocationDto;

import java.util.List;

public interface LocationService {
    List<LocationDto> getAll();
    LocationDto getById(Long id);
}