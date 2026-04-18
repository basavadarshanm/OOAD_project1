package com.onlinebanking.strategy;

import java.math.BigDecimal;
import java.util.Optional;

import com.onlinebanking.dto.TransferRequestDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Strategy Pattern: transfer-specific payment algorithm.
 */
public class TransferPaymentStrategy implements PaymentStrategy<TransferRequestDto, Transaction> {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferPaymentStrategy(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction execute(TransferRequestDto request) {
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Optional<Account> fromOpt = accountRepository.findByAccountNumber(request.fromAccountNumber());
        Optional<Account> toOpt;
        if (request.recipientIdentifier() != null && request.recipientIdentifier().matches("\\d{10}")) {
            toOpt = accountRepository.findByPhoneNumber(request.recipientIdentifier());
        } else {
            toOpt = accountRepository.findByAccountNumber(request.recipientIdentifier());
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

        accountRepository.updateBalance(from.getId(), from.getBalance().subtract(amount));
        accountRepository.updateBalance(to.getId(), to.getBalance().add(amount));

        String description = request.description() == null || request.description().isBlank()
                ? "Transfer to " + request.recipientIdentifier()
                : request.description();

        return transactionRepository.create(from.getId(), to.getId(), "TRANSFER", amount, description);
    }
}
