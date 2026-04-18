package com.onlinebanking.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.onlinebanking.model.Transaction;

/**
 * Builder Pattern: builds immutable Transaction objects with readable fluent steps.
 */
public class TransactionBuilder {
    private long id;
    private long fromAccountId;
    private Long toAccountId;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private String status = "COMPLETED";
    private LocalDateTime createdAt = LocalDateTime.now();

    public TransactionBuilder id(long id) {
        this.id = id;
        return this;
    }

    public TransactionBuilder fromAccountId(long fromAccountId) {
        this.fromAccountId = fromAccountId;
        return this;
    }

    public TransactionBuilder toAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
        return this;
    }

    public TransactionBuilder transactionType(String transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public TransactionBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder description(String description) {
        this.description = description;
        return this;
    }

    public TransactionBuilder status(String status) {
        this.status = status;
        return this;
    }

    public TransactionBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Transaction build() {
        return new Transaction(id, fromAccountId, toAccountId, transactionType, amount, description, status, createdAt);
    }
}
