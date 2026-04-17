package com.onlinebanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final long id;
    private final long fromAccountId;
    private final Long toAccountId; // nullable for bill payments
    private final String transactionType; // TRANSFER, BILL_PAYMENT, DEPOSIT, WITHDRAWAL
    private final BigDecimal amount;
    private final String description;
    private final String status; // PENDING, COMPLETED, FAILED
    private final LocalDateTime createdAt;

    public Transaction(long id, long fromAccountId, Long toAccountId, String transactionType,
                      BigDecimal amount, String description, String status, LocalDateTime createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Amount: %s | Status: %s | %s",
                createdAt.toLocalDate(),
                transactionType,
                amount,
                status,
                description);
    }
}
