package com.onlinebanking.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.onlinebanking.builder.TransactionBuilder;
import com.onlinebanking.model.Transaction;

/**
 * Factory Pattern: central place to create Transaction domain objects.
 */
public final class TransactionFactory {
    private TransactionFactory() {
    }

    public static Transaction completed(
            long id,
            long fromAccountId,
            Long toAccountId,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDateTime createdAt
    ) {
        return withStatus(id, fromAccountId, toAccountId, transactionType, amount, description, "COMPLETED", createdAt);
        }

        public static Transaction withStatus(
            long id,
            long fromAccountId,
            Long toAccountId,
            String transactionType,
            BigDecimal amount,
            String description,
            String status,
            LocalDateTime createdAt
        ) {
        return new TransactionBuilder()
                .id(id)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .transactionType(transactionType)
                .amount(amount)
                .description(description)
            .status(status)
                .createdAt(createdAt)
                .build();
    }
}
