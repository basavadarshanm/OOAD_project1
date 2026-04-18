package com.onlinebanking.strategy;

import java.math.BigDecimal;
import java.util.Optional;

import com.onlinebanking.dto.BillPaymentRequestDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.BillPayment;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BillPaymentRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Strategy Pattern: bill-payment-specific payment algorithm.
 */
public class BillPaymentStrategy implements PaymentStrategy<BillPaymentRequestDto, BillPayment> {
    private final AccountRepository accountRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentStrategy(
            AccountRepository accountRepository,
            BillPaymentRepository billPaymentRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public BillPayment execute(BillPaymentRequestDto request) {
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.fromAccountNumber());
        if (accountOpt.isEmpty()) {
            throw new IllegalStateException("Account not found");
        }

        Account account = accountOpt.get();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for bill payment");
        }

        Transaction tx = transactionRepository.create(
                account.getId(),
                null,
                "BILL_PAYMENT",
                amount,
                "Bill payment to " + request.billerName()
        );

        accountRepository.updateBalance(account.getId(), account.getBalance().subtract(amount));

        return billPaymentRepository.create(
                account.getId(),
                request.billerName(),
                amount,
                request.billReference(),
                request.dueDate(),
                tx.getId()
        );
    }
}
