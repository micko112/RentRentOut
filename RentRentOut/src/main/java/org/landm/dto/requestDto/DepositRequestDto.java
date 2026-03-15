package org.landm.dto.requestDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.landm.entity.Enums.Currency;

import java.math.BigDecimal;

@Setter
@Getter
public class DepositRequestDto {

	@NotNull(message = "Amount is required!")
	@DecimalMin(value = "100", message = "Minimal amount is 100")
	private BigDecimal amount;
	
	private Currency currency = Currency.RSD;
	
	public DepositRequestDto() {}
	
	public DepositRequestDto(BigDecimal amount) {
		this.amount = amount;
	}




	
}
