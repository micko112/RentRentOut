package org.landm.entity;

import jakarta.persistence.*;
import org.landm.entity.Enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transaction")
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Pozitivan = dopuna, negativan = trošenje */
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(length = 255)
    private String description;

    /** ID oglasa ili promocije (opcionalno) */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CreditTransaction() {}

    public CreditTransaction(User user, BigDecimal amount, TransactionType transactionType,
                             String description, Long referenceId) {
        this.user = user;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getDescription() { return description; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
