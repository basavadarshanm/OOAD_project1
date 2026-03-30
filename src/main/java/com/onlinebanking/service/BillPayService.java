package com.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.onlinebanking.model.Account;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Simplified bill payment service.
 */
public class BillPayService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BillPayService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void payBill(String fromAccountNumber, BigDecimal amount, String biller) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Optional<Account> fromOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        if (fromOpt.isEmpty()) {
            throw new IllegalStateException("Account not found");
        }
        Account from = fromOpt.get();
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        accountRepository.updateBalance(from.getId(), from.getBalance().subtract(amount));
        transactionRepository.record(new com.onlinebanking.model.Transaction(0, from.getId(), "DEBIT", amount, LocalDateTime.now(),
                "Bill pay: " + biller));
    }
}
