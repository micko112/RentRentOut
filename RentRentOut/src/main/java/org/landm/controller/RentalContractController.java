package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.RentalContractDto;
import org.landm.dto.requestDto.CreateRentalContractRequestDto;

import org.landm.service.RentalContractService;
import org.landm.service.impl.RentalContractServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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


}
