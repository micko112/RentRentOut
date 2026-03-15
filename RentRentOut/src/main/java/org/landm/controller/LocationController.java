package org.landm.controller;

import org.landm.dto.LocationDto;
import org.landm.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }
}