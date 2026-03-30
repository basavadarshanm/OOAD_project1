package com.onlinebanking.model;

public class Beneficiary {
    private final long id;
    private final long userId;
    private final String name;
    private final String accountNumber;
    private final String bank;

    public Beneficiary(long id, long userId, String name, String accountNumber, String bank) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.bank = bank;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBank() {
        return bank;
    }
}
