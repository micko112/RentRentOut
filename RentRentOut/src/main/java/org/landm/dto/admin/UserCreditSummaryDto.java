package org.landm.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCreditSummaryDto {

    private Long userId;
    private String fullName;
    private String email;
    private BigDecimal currentBalance;
    private BigDecimal totalSpent;
    private BigDecimal totalTopups;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastTransactionAt;

    public UserCreditSummaryDto() {}

    public UserCreditSummaryDto(Long userId, String fullName, String email,
                                BigDecimal currentBalance, BigDecimal totalSpent,
                                BigDecimal totalTopups, LocalDateTime lastTransactionAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.currentBalance = currentBalance;
        this.totalSpent = totalSpent;
        this.totalTopups = totalTopups;
        this.lastTransactionAt = lastTransactionAt;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public BigDecimal getTotalTopups() { return totalTopups; }
    public LocalDateTime getLastTransactionAt() { return lastTransactionAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public void setTotalTopups(BigDecimal totalTopups) { this.totalTopups = totalTopups; }
    public void setLastTransactionAt(LocalDateTime lastTransactionAt) { this.lastTransactionAt = lastTransactionAt; }
}
