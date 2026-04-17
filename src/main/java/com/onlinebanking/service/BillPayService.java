package com.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.onlinebanking.model.Account;
import com.onlinebanking.model.BillPayment;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BillPaymentRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Handles bill payments and tracks bill payment records.
 */
public class BillPayService {
    private final AccountRepository accountRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final TransactionRepository transactionRepository;

    public BillPayService(AccountRepository accountRepository, BillPaymentRepository billPaymentRepository,
                         TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Pay a bill from an account
     */
    public BillPayment payBill(String fromAccountNumber, String billerName, BigDecimal amount,
                               String billReference, LocalDate dueDate) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Optional<Account> accountOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        if (accountOpt.isEmpty()) {
            throw new IllegalStateException("Account not found");
        }

        Account account = accountOpt.get();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for bill payment");
        }

        // Create transaction
        Transaction tx = transactionRepository.create(
                account.getId(),
                null,
                "BILL_PAYMENT",
                amount,
                "Bill payment to " + billerName
        );

        // Update account balance
        accountRepository.updateBalance(account.getId(), account.getBalance().subtract(amount));

        // Record bill payment
        return billPaymentRepository.create(account.getId(), billerName, amount, billReference, dueDate, tx.getId());
    }

    /**
     * Get all bill payments for an account
     */
    public List<BillPayment> getBillPayments(long accountId) {
        return billPaymentRepository.findByAccountId(accountId);
    }

    /**
     * Get a specific bill payment
     */
    public Optional<BillPayment> getBillPayment(long billPaymentId) {
        return billPaymentRepository.findById(billPaymentId);
    }

    /**
     * Check if bill payment is possible
     */
    public boolean canPayBill(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return false;
        }
        return accountOpt.get().getBalance().compareTo(amount) >= 0;
    }
}
