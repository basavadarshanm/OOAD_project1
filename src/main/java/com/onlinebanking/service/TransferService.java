package com.onlinebanking.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.onlinebanking.dto.TransferRequestDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.strategy.PaymentStrategyFactory;
import com.onlinebanking.strategy.PaymentStrategyFactory.PaymentType;

/**
 * Service Layer Pattern: delegates transfer business flow to strategy implementation.
 */
public class TransferService {
    private final AccountRepository accountRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public TransferService(AccountRepository accountRepository, PaymentStrategyFactory paymentStrategyFactory) {
        this.accountRepository = accountRepository;
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    /**
     * Transfer money using configured payment strategy.
     * @throws IllegalArgumentException if amount is invalid
     * @throws IllegalStateException if transfer fails
     */
    public Transaction transfer(String fromAccountNumber, String recipientIdentifier, BigDecimal amount, String description) {
        TransferRequestDto request = new TransferRequestDto(fromAccountNumber, recipientIdentifier, amount, description);
        return paymentStrategyFactory
                .<TransferRequestDto, Transaction>getStrategy(PaymentType.TRANSFER)
                .execute(request);
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
