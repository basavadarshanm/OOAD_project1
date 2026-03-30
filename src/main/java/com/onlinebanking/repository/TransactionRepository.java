package com.onlinebanking.repository;

import java.util.List;

import com.onlinebanking.model.Transaction;

public interface TransactionRepository {
    List<Transaction> findByAccountId(long accountId, int limit);
    void record(Transaction tx);
}
