package org.landm.dto.rentalContract;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import org.landm.entity.Enums.ContractStatus;

public class UpdateRentalContractStatusRequestDto {
    @NotNull
    private ContractStatus newStatus;
    
    @Positive
    private BigDecimal newPrice;

    public ContractStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ContractStatus newStatus) {
        this.newStatus = newStatus;
    }

	public BigDecimal getNewPrice() {
		return newPrice;
	}

	public void setNewPrice(BigDecimal newPrice) {
		this.newPrice = newPrice;
	}
    
    
}
