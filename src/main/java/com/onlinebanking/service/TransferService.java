package com.onlinebanking.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

import javax.sql.DataSource;

import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Handles money transfers between accounts with balance validation.
 */
public class TransferService {
    private final DataSource dataSource;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(DataSource dataSource, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Transfer money between two accounts atomically.
     * @throws IllegalArgumentException if amount is invalid
     * @throws IllegalStateException if transfer fails
     */
    public Transaction transfer(String fromAccountNumber, String recipientIdentifier, BigDecimal amount, String description) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Optional<Account> fromOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        Optional<Account> toOpt;
        if (recipientIdentifier != null && recipientIdentifier.matches("\\d{10}")) {
            toOpt = accountRepository.findByPhoneNumber(recipientIdentifier);
        } else {
            toOpt = accountRepository.findByAccountNumber(recipientIdentifier);
        }

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            throw new IllegalStateException("One or both accounts not found");
        }

        Account from = fromOpt.get();
        Account to = toOpt.get();

        if (from.getId() == to.getId()) {
            throw new IllegalStateException("Cannot transfer to the same account");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds. Available: " + from.getBalance());
        }

        // Use JDBC transaction for atomicity
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update balances
                BigDecimal fromNew = from.getBalance().subtract(amount);
                BigDecimal toNew = to.getBalance().add(amount);

                accountRepository.updateBalance(from.getId(), fromNew);
                accountRepository.updateBalance(to.getId(), toNew);

                // Create transaction record
                Transaction tx = transactionRepository.create(
                        from.getId(),
                        to.getId(),
                        "TRANSFER",
                        amount,
                        description != null ? description : "Transfer to " + recipientIdentifier
                );

                conn.commit();
                return tx;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Transfer failed: " + e.getMessage(), e);
        }
    }

    /**
     * Check if transfer is possible (balance sufficient)
     */
    public boolean canTransfer(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return false;
        }
        return accountOpt.get().getBalance().compareTo(amount) >= 0;
    }
}
