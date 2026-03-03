package org.landm.controller;

import jakarta.validation.Valid;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;

import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.service.RentalContractService;
import org.landm.service.impl.RentalContractServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rental-contract")
public class RentalContractController {

    private final RentalContractService service;

    public RentalContractController(RentalContractService service) {
        this.service = service;

    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<RentalContractDto> createRentalContract(@Valid @RequestBody CreateRentalContractRequestDto req,
                                                                  Authentication auth){
		long userId = Long.parseLong(auth.getName());
		System.out.println("Calling service");
	    RentalContractDto newRental = service.create(req, userId);
	    return new ResponseEntity<>(newRental, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<RentalContractDto> getRentalContractById(@PathVariable long rentalId){
		return new ResponseEntity<>(service.getRentalContractById(rentalId), HttpStatus.OK);
	}
	
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	@GetMapping("/my-contracts")
	public ResponseEntity<List<RentalContractDto>> getAllRentalContracts(Authentication auth){
		long userId = Long.parseLong(auth.getName());
		return new ResponseEntity<>(service.getAll(userId), HttpStatus.OK);
	}

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	@GetMapping("/search")
	public ResponseEntity<Map<String, Page<RentalContractDto>>> searchContracts(Authentication auth, 
			@ModelAttribute RentalContractSearchDto searchDto){
		long userId = Long.parseLong(auth.getName());
		boolean isAdmin = auth.getAuthorities()
				.stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		System.out.println(isAdmin);
		Map res = new HashMap<>();
		res.put("Contracts: ", service.search(userId, isAdmin, searchDto));
		return new ResponseEntity<>(res, HttpStatus.OK);
	} 
	
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	@PatchMapping("/{id}/status")
	public ResponseEntity<RentalContractDto> updateStatus(@PathVariable long id,
	                                                      @Valid @RequestBody UpdateRentalContractStatusRequestDto req, 
	                                                      Authentication auth){
        long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(service.updateStatus(id, req, userId));
	}
	
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteRentalContract(@PathVariable long id, Authentication auth){
		long userId = Long.parseLong(auth.getName());
		return new ResponseEntity<>(service.delete(userId, id), HttpStatus.OK);
	}
}
