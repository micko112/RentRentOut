package org.landm.service.impl;

import org.landm.dto.LocationDto;
import org.landm.entity.Location;
import org.landm.mapper.LocationMapper;
import org.landm.repository.LocationRepository;
import org.landm.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public List<LocationDto> getAll() {
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Override
    public LocationDto getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return locationMapper.toDto(location);
    }
}