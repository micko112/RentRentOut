package org.landm.service;

import org.landm.dto.RentalContractDto;
import org.landm.dto.requestDto.CreateRentalContractRequestDto;

public interface RentalContractService {
    public RentalContractDto create(CreateRentalContractRequestDto req, String token);
}
