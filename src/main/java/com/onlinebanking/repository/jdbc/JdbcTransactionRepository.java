package com.onlinebanking.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT id, account_id, type, amount, occurred_at, description FROM transactions " +
                "WHERE account_id = ? ORDER BY occurred_at DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setInt(2, limit);
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
    public void record(Transaction tx) {
        String sql = "INSERT INTO transactions (account_id, type, amount, occurred_at, description) VALUES (?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tx.getAccountId());
            ps.setString(2, tx.getType());
            ps.setBigDecimal(3, tx.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(tx.getOccurredAt()));
            ps.setString(5, tx.getDescription());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to record transaction", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws Exception {
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("account_id"),
                rs.getString("type"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("occurred_at").toLocalDateTime(),
                rs.getString("description")
        );
    }
}
