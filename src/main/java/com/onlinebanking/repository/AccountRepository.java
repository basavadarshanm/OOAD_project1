package com.onlinebanking.repository;

import com.onlinebanking.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByPhoneNumber(String phoneNumber);
    Optional<String> findPhoneByAccountNumber(String accountNumber);
    Account create(long userId, String accountNumber, java.math.BigDecimal initialBalance);
    Account create(long userId, String accountNumber, String phoneNumber, java.math.BigDecimal initialBalance);
    void updateBalance(long accountId, java.math.BigDecimal newBalance);
}
