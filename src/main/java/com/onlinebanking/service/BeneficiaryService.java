package com.onlinebanking.service;

import java.util.List;

import com.onlinebanking.model.Beneficiary;
import com.onlinebanking.repository.BeneficiaryRepository;

public class BeneficiaryService {
    private final BeneficiaryRepository repository;

    public BeneficiaryService(BeneficiaryRepository repository) {
        this.repository = repository;
    }

    public List<Beneficiary> getBeneficiaries(long userId) {
        return repository.findByUserId(userId);
    }

    public void addBeneficiary(long userId, String accountNumber, String name) {
        Beneficiary beneficiary = new Beneficiary(0, userId, name, accountNumber, "");
        repository.add(beneficiary);
    }

    public void add(Beneficiary beneficiary) {
        repository.add(beneficiary);
    }

    public List<Beneficiary> list(long userId) {
        return repository.findByUserId(userId);
    }
}
