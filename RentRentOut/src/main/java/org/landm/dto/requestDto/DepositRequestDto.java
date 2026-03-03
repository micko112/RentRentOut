package org.landm.dto.requestDto;

import java.math.BigDecimal;

import org.landm.entity.Enums.Currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class DepositRequestDto {

	@NotNull(message = "Amount is required!")
	@DecimalMin(value = "100", message = "Minimal amount is 100")
	private BigDecimal amount;
	
	private Currency currency = Currency.RSD;
	
	public DepositRequestDto() {}
	
	public DepositRequestDto(BigDecimal amount) {
		this.amount = amount;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Currency getPriceCurrency() {
		return currency;
	}

	public void setPriceCurrency(Currency currency) {
		this.currency = currency;
	}
	
	
	
}
