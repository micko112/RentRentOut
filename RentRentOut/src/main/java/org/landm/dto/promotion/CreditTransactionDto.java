package org.landm.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.landm.entity.CreditTransaction;
import org.landm.entity.Enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditTransactionDto {
    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private Long referenceId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static CreditTransactionDto from(CreditTransaction tx) {
        CreditTransactionDto dto = new CreditTransactionDto();
        dto.id = tx.getId();
        dto.amount = tx.getAmount();
        dto.transactionType = tx.getTransactionType();
        dto.description = tx.getDescription();
        dto.referenceId = tx.getReferenceId();
        dto.createdAt = tx.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getDescription() { return description; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
