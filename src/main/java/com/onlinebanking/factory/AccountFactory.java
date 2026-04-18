package com.onlinebanking.factory;

import java.math.BigDecimal;

import com.onlinebanking.model.Account;

/**
 * Factory Pattern: creates Account objects with shared defaults.
 */
public final class AccountFactory {
    private AccountFactory() {
    }

    public static Account create(long id, long userId, String accountNumber, BigDecimal balance) {
        return new Account(id, userId, accountNumber, balance);
    }
}
