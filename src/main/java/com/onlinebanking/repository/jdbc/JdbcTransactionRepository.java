package com.onlinebanking.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.onlinebanking.model.Transaction;
import com.onlinebanking.repository.TransactionRepository;

public class JdbcTransactionRepository implements TransactionRepository {
    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Transaction> findByAccountId(long accountId, int limit) {
        String sql = "SELECT id, from_account_id, to_account_id, transaction_type, amount, description, status, created_at " +
                "FROM transactions WHERE from_account_id = ? OR to_account_id = ? " +
                "ORDER BY created_at DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setLong(2, accountId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query transactions", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT id, from_account_id, to_account_id, transaction_type, amount, description, status, created_at " +
                "FROM transactions ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query all transactions", e);
        }
    }

    @Override
    public Optional<Transaction> findById(long id) {
        String sql = "SELECT id, from_account_id, to_account_id, transaction_type, amount, description, status, created_at " +
                "FROM transactions WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query transaction", e);
        }
    }

    @Override
    public Transaction create(long fromAccountId, Long toAccountId, String transactionType,
                             java.math.BigDecimal amount, String description) {
        String sql = "INSERT INTO transactions (from_account_id, to_account_id, transaction_type, amount, description, status) " +
                "VALUES (?, ?, ?, ?, ?, 'COMPLETED')";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, fromAccountId);
            if (toAccountId != null) {
                ps.setLong(2, toAccountId);
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, transactionType);
            ps.setBigDecimal(4, amount);
            ps.setString(5, description);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Transaction(id, fromAccountId, toAccountId, transactionType, amount, description, "COMPLETED",
                            java.time.LocalDateTime.now());
                }
            }
            throw new IllegalStateException("Failed to create transaction: no generated key returned");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create transaction", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws Exception {
        Long toAccountId = rs.getLong("to_account_id");
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("from_account_id"),
                rs.wasNull() ? null : toAccountId,
                rs.getString("transaction_type"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
