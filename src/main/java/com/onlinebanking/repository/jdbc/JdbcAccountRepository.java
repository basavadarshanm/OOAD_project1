package com.onlinebanking.repository.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.onlinebanking.model.Account;
import com.onlinebanking.repository.AccountRepository;

public class JdbcAccountRepository implements AccountRepository {
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Account> findByUserId(long userId) {
        String sql = "SELECT id, user_id, account_number, balance FROM accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }
            return accounts;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query accounts", e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT id, user_id, account_number, balance FROM accounts WHERE account_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query account", e);
        }
    }

    @Override
    public Optional<Account> findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT id, user_id, account_number, balance FROM accounts WHERE phone_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query account by phone", e);
        }
    }

    @Override
    public Optional<String> findPhoneByAccountNumber(String accountNumber) {
        String sql = "SELECT phone_number FROM accounts WHERE account_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String value = rs.getString("phone_number");
                    return Optional.ofNullable(value);
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query phone by account", e);
        }
    }

    @Override
    public Account create(long userId, String accountNumber, BigDecimal initialBalance) {
        return create(userId, accountNumber, null, initialBalance);
    }

    @Override
    public Account create(long userId, String accountNumber, String phoneNumber, BigDecimal initialBalance) {
        String sql = "INSERT INTO accounts (user_id, account_number, balance) VALUES (?, ?, ?)";
        String sqlWithPhone = "INSERT INTO accounts (user_id, account_number, phone_number, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(phoneNumber == null ? sql : sqlWithPhone, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, accountNumber);
            if (phoneNumber == null) {
                ps.setBigDecimal(3, initialBalance);
            } else {
                ps.setString(3, phoneNumber);
                ps.setBigDecimal(4, initialBalance);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Account(keys.getLong(1), userId, accountNumber, initialBalance);
                }
            }
            throw new IllegalStateException("Failed to create account: no generated key returned");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create account", e);
        }
    }

    @Override
    public void updateBalance(long accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update balance", e);
        }
    }

    private Account mapRow(ResultSet rs) throws Exception {
        return new Account(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("account_number"),
                rs.getBigDecimal("balance")
        );
    }
}
