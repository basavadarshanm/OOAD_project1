package com.onlinebanking.repository;

import com.onlinebanking.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    void updateBalance(long accountId, java.math.BigDecimal newBalance);
}
