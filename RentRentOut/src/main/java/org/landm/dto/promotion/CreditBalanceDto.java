package org.landm.dto.promotion;

import java.math.BigDecimal;

public class CreditBalanceDto {
    private BigDecimal balance;

    public CreditBalanceDto(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() { return balance; }
}
