package com.onlinebanking.strategy;

/**
 * Strategy Pattern: contract for payment execution variants.
 */
public interface PaymentStrategy<TRequest, TResult> {
    TResult execute(TRequest request);
}
