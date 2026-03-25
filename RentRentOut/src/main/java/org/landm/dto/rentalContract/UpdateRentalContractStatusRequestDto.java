package org.landm.dto.rentalContract;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.landm.entity.Enums.ContractStatus;

public class UpdateRentalContractStatusRequestDto {
    @NotNull
    private ContractStatus newStatus;

    @Positive
    private BigDecimal newPrice;

    @FutureOrPresent
    private LocalDate newStartDate;

    @FutureOrPresent
    private LocalDate newEndDate;

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

    public LocalDate getNewStartDate() {
        return newStartDate;
    }

    public void setNewStartDate(LocalDate newStartDate) {
        this.newStartDate = newStartDate;
    }

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }
}
