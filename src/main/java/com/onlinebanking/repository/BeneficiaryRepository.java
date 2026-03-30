package com.onlinebanking.repository;

import java.util.List;

import com.onlinebanking.model.Beneficiary;

public interface BeneficiaryRepository {
    List<Beneficiary> findByUserId(long userId);
    void add(Beneficiary beneficiary);
}
