package com.onlinebanking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    public String getPhoneNumber(String accountNumber) {
        return accountRepository.findPhoneByAccountNumber(accountNumber).orElse("Not set");
    }

    public Account createAccount(long userId, BigDecimal initialAmount) {
        return createAccount(userId, null, initialAmount, null);
    }

    public Account createAccount(long userId, String phoneNumber, BigDecimal initialAmount, String requestedAccountNumber) {
        if (initialAmount == null || initialAmount.signum() < 0) {
            throw new IllegalArgumentException("Initial amount cannot be negative");
        }

        List<Account> existing = accountRepository.findByUserId(userId);
        if (!existing.isEmpty()) {
            throw new IllegalStateException("An account already exists for this user");
        }

        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits");
        }
        if (accountRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalStateException("Phone number is already linked to another account");
        }

        String accountNumber = requestedAccountNumber;
        if (accountNumber == null || accountNumber.isBlank()) {
            accountNumber = generateUniqueAccountNumber();
        }
        if (!accountNumber.matches("\\d{8}")) {
            throw new IllegalArgumentException("Account ID must be exactly 8 digits");
        }
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new IllegalStateException("Generated account ID collided. Please retry.");
        }

        Account created = accountRepository.create(userId, accountNumber, phoneNumber, initialAmount);

        if (initialAmount.signum() > 0) {
            transactionRepository.create(
                    created.getId(),
                    null,
                    "DEPOSIT",
                    initialAmount,
                    "Initial account funding"
            );
        }

        return created;
    }

    public Account addMoney(Account account, BigDecimal amount) {
        if (account == null) {
            throw new IllegalArgumentException("Account is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        accountRepository.updateBalance(account.getId(), newBalance);
        transactionRepository.create(
                account.getId(),
                null,
                "DEPOSIT",
                amount,
                "Cash deposit"
        );

        return new Account(account.getId(), account.getUserId(), account.getAccountNumber(), newBalance);
    }

    public String generateSuggestedAccountNumber() {
        return generateUniqueAccountNumber();
    }

    private String generateUniqueAccountNumber() {
        for (int i = 0; i < 50; i++) {
            String candidate = String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000));
            if (accountRepository.findByAccountNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique account number");
    }
}
