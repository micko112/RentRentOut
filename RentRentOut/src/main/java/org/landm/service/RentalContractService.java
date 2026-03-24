package org.landm.service;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.RentalContractSearchDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.RentalContract;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RentalContractService {
	
    public RentalContractDto create(CreateRentalContractRequestDto req, Long userId);
    
    public List<RentalContract> findByAdIdAndContractStatusIn(Long adId, List<ContractStatus> statusList);
    
    public RentalContractDto updateStatus(Long contractId,
                        UpdateRentalContractStatusRequestDto req, Long userId);
    
    public RentalContractDto getRentalContractById(Long rentalId, Long requestingUserId);
    
    public Page<RentalContractDto> search(Long userId, boolean isAdmin, RentalContractSearchDto searchDto);
    
    public List<RentalContractDto> getAll(Long userId);
    
    public boolean isActiveOrAccepted(ContractStatus status);
    
    public String delete(Long userId, Long rentalId);
    
    public void markToAdDeleted(Long adId);

    public RentalContractDto blockDates(CreateRentalContractRequestDto req, Long userId);
}
