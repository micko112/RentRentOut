package org.landm.dto.rentalContract;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.landm.entity.Enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;


public class CreateRentalContractRequestDto {
    @NotNull
    private Long adId;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @Positive
    private BigDecimal agreedPrice;
    @NotNull
    private Currency currency;
    @Positive 
    private Long amount;

    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
}
