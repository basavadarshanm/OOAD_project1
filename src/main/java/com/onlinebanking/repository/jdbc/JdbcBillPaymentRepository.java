package com.onlinebanking.repository.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.onlinebanking.model.BillPayment;
import com.onlinebanking.repository.BillPaymentRepository;

public class JdbcBillPaymentRepository implements BillPaymentRepository {
    private final DataSource dataSource;

    public JdbcBillPaymentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<BillPayment> findByAccountId(long accountId) {
        String sql = "SELECT id, account_id, biller_name, amount, bill_reference_number, due_date, paid_date, status, transaction_id " +
                "FROM bill_payments WHERE account_id = ? ORDER BY paid_date DESC";
        List<BillPayment> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query bill payments", e);
        }
    }

    @Override
    public Optional<BillPayment> findById(long id) {
        String sql = "SELECT id, account_id, biller_name, amount, bill_reference_number, due_date, paid_date, status, transaction_id " +
                "FROM bill_payments WHERE id = ?";
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
            throw new IllegalStateException("Failed to query bill payment", e);
        }
    }

    @Override
    public BillPayment create(long accountId, String billerName, java.math.BigDecimal amount,
                             String billReferenceNumber, java.time.LocalDate dueDate, long transactionId) {
        String sql = "INSERT INTO bill_payments (account_id, biller_name, amount, bill_reference_number, due_date, status, transaction_id) " +
                "VALUES (?, ?, ?, ?, ?, 'PAID', ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, accountId);
            ps.setString(2, billerName);
            ps.setBigDecimal(3, amount);
            ps.setString(4, billReferenceNumber);
            ps.setDate(5, Date.valueOf(dueDate));
            ps.setLong(6, transactionId);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new BillPayment(id, accountId, billerName, amount, billReferenceNumber,
                            dueDate, java.time.LocalDateTime.now(), "PAID", transactionId);
                }
            }
            throw new IllegalStateException("Failed to create bill payment: no generated key returned");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create bill payment", e);
        }
    }

    @Override
    public void updateStatus(long billPaymentId, String status) {
        String sql = "UPDATE bill_payments SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, billPaymentId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update bill payment status", e);
        }
    }

    private BillPayment mapRow(ResultSet rs) throws Exception {
        Long transactionId = rs.getLong("transaction_id");
        return new BillPayment(
                rs.getLong("id"),
                rs.getLong("account_id"),
                rs.getString("biller_name"),
                rs.getBigDecimal("amount"),
                rs.getString("bill_reference_number"),
                rs.getDate("due_date").toLocalDate(),
                rs.getTimestamp("paid_date").toLocalDateTime(),
                rs.getString("status"),
                rs.wasNull() ? null : transactionId
        );
    }
}
