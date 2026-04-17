package com.onlinebanking.repository;

import java.util.List;
import java.util.Optional;

import com.onlinebanking.model.Transaction;

public interface TransactionRepository {
    List<Transaction> findByAccountId(long accountId, int limit);
    List<Transaction> findAll();
    Optional<Transaction> findById(long id);
    Transaction create(long fromAccountId, Long toAccountId, String transactionType,
                      java.math.BigDecimal amount, String description);
}
