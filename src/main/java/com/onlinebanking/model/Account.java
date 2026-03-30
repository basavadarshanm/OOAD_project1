package com.onlinebanking.model;

import java.math.BigDecimal;

public class Account {
    private final long id;
    private final long userId;
    private final String accountNumber;
    private final BigDecimal balance;

    public Account(long id, long userId, String accountNumber, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return accountNumber + " | Balance: " + balance;
    }
}
