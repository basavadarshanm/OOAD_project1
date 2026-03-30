package com.onlinebanking.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.sql.DataSource;

import com.onlinebanking.model.Account;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;

/**
 * Handles atomic fund transfers using a single JDBC transaction.
 */
public class TransferService {
    private final DataSource dataSource;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(DataSource dataSource, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Optional<Account> fromOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        Optional<Account> toOpt = accountRepository.findByAccountNumber(toAccountNumber);
        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            throw new IllegalStateException("Account not found");
        }
        Account from = fromOpt.get();
        Account to = toOpt.get();
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal fromNew = from.getBalance().subtract(amount);
        BigDecimal toNew = to.getBalance().add(amount);

        String updateSql = "UPDATE accounts SET balance = ? WHERE id = ?";
        String txSql = "INSERT INTO transactions (account_id, type, amount, occurred_at, description) VALUES (?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (var update = conn.prepareStatement(updateSql); var txStmt = conn.prepareStatement(txSql)) {
                update.setBigDecimal(1, fromNew);
                update.setLong(2, from.getId());
                update.executeUpdate();

                update.setBigDecimal(1, toNew);
                update.setLong(2, to.getId());
                update.executeUpdate();

                LocalDateTime now = LocalDateTime.now();

                txStmt.setLong(1, from.getId());
                txStmt.setString(2, "DEBIT");
                txStmt.setBigDecimal(3, amount);
                txStmt.setObject(4, now);
                txStmt.setString(5, "Transfer to " + to.getAccountNumber());
                txStmt.executeUpdate();

                txStmt.setLong(1, to.getId());
                txStmt.setString(2, "CREDIT");
                txStmt.setBigDecimal(3, amount);
                txStmt.setObject(4, now);
                txStmt.setString(5, "Transfer from " + from.getAccountNumber());
                txStmt.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Transfer failed", e);
        }
    }
}
