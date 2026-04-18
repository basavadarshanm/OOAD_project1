package com.onlinebanking.strategy;

import com.onlinebanking.dto.BillPaymentRequestDto;
import com.onlinebanking.dto.TransferRequestDto;
import com.onlinebanking.model.BillPayment;
import com.onlinebanking.model.Transaction;

/**
 * Factory Pattern: provides payment strategy instances by payment type.
 */
public class PaymentStrategyFactory {
    public enum PaymentType {
        TRANSFER,
        BILL_PAYMENT
    }

    private final PaymentStrategy<TransferRequestDto, Transaction> transferStrategy;
    private final PaymentStrategy<BillPaymentRequestDto, BillPayment> billPaymentStrategy;

    public PaymentStrategyFactory(
            PaymentStrategy<TransferRequestDto, Transaction> transferStrategy,
            PaymentStrategy<BillPaymentRequestDto, BillPayment> billPaymentStrategy
    ) {
        this.transferStrategy = transferStrategy;
        this.billPaymentStrategy = billPaymentStrategy;
    }

    @SuppressWarnings("unchecked")
    public <TRequest, TResult> PaymentStrategy<TRequest, TResult> getStrategy(PaymentType type) {
        if (type == PaymentType.TRANSFER) {
            return (PaymentStrategy<TRequest, TResult>) transferStrategy;
        }
        return (PaymentStrategy<TRequest, TResult>) billPaymentStrategy;
    }
}
