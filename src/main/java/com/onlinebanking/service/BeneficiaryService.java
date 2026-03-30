package com.onlinebanking.service;

import java.util.List;

import com.onlinebanking.model.Beneficiary;
import com.onlinebanking.repository.BeneficiaryRepository;

public class BeneficiaryService {
    private final BeneficiaryRepository repository;

    public BeneficiaryService(BeneficiaryRepository repository) {
        this.repository = repository;
    }

    public List<Beneficiary> list(long userId) {
        return repository.findByUserId(userId);
    }

    public void add(Beneficiary beneficiary) {
        repository.add(beneficiary);
    }
}
