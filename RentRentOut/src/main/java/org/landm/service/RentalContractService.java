package org.landm.service;

import org.landm.dto.rentalContract.RentalContractDto;

import java.util.List;

import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;

public interface RentalContractService {
    public RentalContractDto create(CreateRentalContractRequestDto req, String token);
    public RentalContractDto updateStatus(long contractId,
                        UpdateRentalContractStatusRequestDto req,
<<<<<<< HEAD
                        String token);
    
    public RentalContractDto getRentalContractById(long rentalId);
    
    public List<RentalContractDto> getAll(long userId);
    
=======
                        long userId);
>>>>>>> 60c6d62dce84a24d7dab6618c77adacc20ad7467
}
