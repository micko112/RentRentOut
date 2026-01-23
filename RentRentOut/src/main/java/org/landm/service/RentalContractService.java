package org.landm.service;

import org.landm.dto.rentalContract.RentalContractDto;

import java.util.List;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Enums.ContractStatus;

public interface RentalContractService {
	
    public RentalContractDto create(CreateRentalContractRequestDto req, long userId);
    
    public RentalContractDto updateStatus(long contractId,
                        UpdateRentalContractStatusRequestDto req, long userId);
    
    public RentalContractDto getRentalContractById(long rentalId);
    
    public List<RentalContractDto> search(String term, long userId, boolean isAdmin);
    
    public List<RentalContractDto> getAll(long userId);
    
    public boolean isActiveOrAccepted(ContractStatus status);
    
    public String delete(long userId, long rentalId);
    
    public void markToAdDeleted(long adId);
}
