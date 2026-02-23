package org.landm.dto.requestDto;

import java.math.BigDecimal;

import org.landm.entity.Enums.PriceCurrency;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DepositRequestDto {

	@NotNull(message = "Amount is required!")
	@DecimalMin(value = "100", message = "Minimal amount is 100")
	private BigDecimal amount;
	
	private PriceCurrency priceCurrency = PriceCurrency.RSD;
	
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

	public PriceCurrency getPriceCurrency() {
		return priceCurrency;
	}

	public void setPriceCurrency(PriceCurrency priceCurrency) {
		this.priceCurrency = priceCurrency;
	}
	
	
	
}
