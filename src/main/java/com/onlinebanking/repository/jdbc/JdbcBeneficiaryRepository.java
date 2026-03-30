package com.onlinebanking.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.onlinebanking.model.Beneficiary;
import com.onlinebanking.repository.BeneficiaryRepository;

public class JdbcBeneficiaryRepository implements BeneficiaryRepository {
    private final DataSource dataSource;

    public JdbcBeneficiaryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Beneficiary> findByUserId(long userId) {
        String sql = "SELECT id, user_id, name, account_number, bank FROM beneficiaries WHERE user_id = ?";
        List<Beneficiary> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to query beneficiaries", e);
        }
    }

    @Override
    public void add(Beneficiary beneficiary) {
        String sql = "INSERT INTO beneficiaries (user_id, name, account_number, bank) VALUES (?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, beneficiary.getUserId());
            ps.setString(2, beneficiary.getName());
            ps.setString(3, beneficiary.getAccountNumber());
            ps.setString(4, beneficiary.getBank());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save beneficiary", e);
        }
    }

    private Beneficiary mapRow(ResultSet rs) throws Exception {
        return new Beneficiary(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("account_number"),
                rs.getString("bank")
        );
    }
}
