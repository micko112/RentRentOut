package org.landm.dto.rentalContract;

import jakarta.validation.constraints.NotNull;
import org.landm.entity.Enums.ContractStatus;

public class UpdateRentalContractStatusRequestDto {
    @NotNull
    private ContractStatus newStatus;

    public ContractStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ContractStatus newStatus) {
        this.newStatus = newStatus;
    }
}
