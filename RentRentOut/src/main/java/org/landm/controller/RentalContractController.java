package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;

import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.service.RentalContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rental-contract")
public class RentalContractController {

    private final RentalContractService service;

    public RentalContractController(RentalContractService service) {
        this.service = service;

    }
@PostMapping("/create")
    public ResponseEntity<RentalContractDto> createRentalContract(@Valid @RequestBody CreateRentalContractRequestDto req,
                                                                  @RequestHeader("Authorization") String authHeader){
    RentalContractDto newRental = service.create(req, authHeader);
    return new ResponseEntity<>(newRental, HttpStatus.CREATED);
    }

@PatchMapping("/{id}/status")
public ResponseEntity<RentalContractDto> updateStatus(@PathVariable long id,
                                                      @Valid @RequestBody UpdateRentalContractStatusRequestDto req,
                                                      Authentication auth){
        long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(service.updateStatus(id, req, userId));
}
}
