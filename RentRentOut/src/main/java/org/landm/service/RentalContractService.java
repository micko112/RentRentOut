package org.landm.service;

import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;

import java.util.List;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.RentalContract;
import org.landm.entity.Enums.ContractStatus;
import org.springframework.data.domain.Page;

public interface RentalContractService {
	
    public RentalContractDto create(CreateRentalContractRequestDto req, long userId);
    
    public List<RentalContract> findByAdIdAndContractStatusIn(long adId, List<ContractStatus> statusList);
    
    public RentalContractDto updateStatus(long contractId,
                        UpdateRentalContractStatusRequestDto req, long userId);
    
    public RentalContractDto getRentalContractById(long rentalId);
    
    public Page<RentalContractDto> search(long userId, boolean isAdmin, RentalContractSearchDto searchDto);
    
    public List<RentalContractDto> getAll(long userId);
    
    public boolean isActiveOrAccepted(ContractStatus status);
    
    public String delete(long userId, long rentalId);
    
    public void markToAdDeleted(long adId);


}
