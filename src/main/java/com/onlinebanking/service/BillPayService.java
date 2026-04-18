package com.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.onlinebanking.dto.BillPaymentRequestDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.BillPayment;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BillPaymentRepository;
import com.onlinebanking.strategy.PaymentStrategyFactory;
import com.onlinebanking.strategy.PaymentStrategyFactory.PaymentType;

/**
 * Service Layer Pattern: delegates bill payment flow to strategy implementation.
 */
public class BillPayService {
    private final AccountRepository accountRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final BillPaymentRepository billPaymentRepository;

    public BillPayService(AccountRepository accountRepository, PaymentStrategyFactory paymentStrategyFactory,
                         BillPaymentRepository billPaymentRepository) {
        this.accountRepository = accountRepository;
        this.paymentStrategyFactory = paymentStrategyFactory;
        this.billPaymentRepository = billPaymentRepository;
    }

    /**
     * Pay a bill from an account
     */
    public BillPayment payBill(String fromAccountNumber, String billerName, BigDecimal amount,
                               String billReference, LocalDate dueDate) {
        BillPaymentRequestDto request = new BillPaymentRequestDto(fromAccountNumber, billerName, amount, billReference, dueDate);
        return paymentStrategyFactory
                .<BillPaymentRequestDto, BillPayment>getStrategy(PaymentType.BILL_PAYMENT)
                .execute(request);
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
