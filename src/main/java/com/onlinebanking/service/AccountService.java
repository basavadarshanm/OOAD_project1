package com.onlinebanking.service;

import java.util.List;

import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Account> getAccounts(long userId) {
        return accountRepository.findByUserId(userId);
    }

    public List<Transaction> getRecentTransactions(long accountId) {
        return transactionRepository.findByAccountId(accountId, 20);
    }
}
