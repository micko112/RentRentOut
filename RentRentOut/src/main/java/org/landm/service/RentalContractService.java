package org.landm.service;

import org.landm.dto.rentalContract.RentalContractDto;
import org.landm.dto.rentalContract.CreateRentalContractRequestDto;
import org.landm.dto.rentalContract.UpdateRentalContractStatusRequestDto;

public interface RentalContractService {
    public RentalContractDto create(CreateRentalContractRequestDto req, String token);
    public RentalContractDto updateStatus(long contractId,
                        UpdateRentalContractStatusRequestDto req,
                        long userId);
}
