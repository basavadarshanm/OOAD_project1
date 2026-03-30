package com.onlinebanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final long id;
    private final long accountId;
    private final String type; // DEBIT or CREDIT
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;
    private final String description;

    public Transaction(long id, long accountId, String type, BigDecimal amount, LocalDateTime occurredAt, String description) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return occurredAt + " - " + type + " - " + amount + " - " + description;
    }
}
