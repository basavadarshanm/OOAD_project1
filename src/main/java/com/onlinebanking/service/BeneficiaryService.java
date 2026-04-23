package com.onlinebanking.service;

import java.util.List;

import com.onlinebanking.model.Account;
import com.onlinebanking.model.Beneficiary;
import com.onlinebanking.model.User;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BeneficiaryRepository;
import com.onlinebanking.repository.UserRepository;

public class BeneficiaryService {
    private final BeneficiaryRepository repository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryService(BeneficiaryRepository repository) {
        this(repository, null, null);
    }

    public BeneficiaryService(BeneficiaryRepository repository, UserRepository userRepository, AccountRepository accountRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public List<Beneficiary> getBeneficiaries(long userId) {
        return repository.findByUserId(userId);
    }

    public void addBeneficiary(long userId, String accountNumber, String name) {
        Beneficiary beneficiary = new Beneficiary(0, userId, name, accountNumber, "");
        repository.add(beneficiary);
    }

    public Beneficiary addBeneficiaryForUser(long ownerUserId, String beneficiaryIdentifier) {
        return addBeneficiaryForUser(ownerUserId, beneficiaryIdentifier, "");
    }

    public Beneficiary addBeneficiaryForUser(long ownerUserId, String beneficiaryIdentifier, String bank) {
        ensureLookupDependencies();

        String normalizedIdentifier = beneficiaryIdentifier == null ? "" : beneficiaryIdentifier.trim();
        if (normalizedIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Beneficiary user ID or phone number is required");
        }

        ResolvedBeneficiary resolvedBeneficiary = resolveBeneficiary(normalizedIdentifier);
        if (resolvedBeneficiary.user().getId() == ownerUserId) {
            throw new IllegalStateException("You cannot add your own account as a beneficiary");
        }

        boolean alreadySaved = repository.findByUserId(ownerUserId).stream()
                .anyMatch(existing -> existing.getAccountNumber().equals(resolvedBeneficiary.account().getAccountNumber()));
        if (alreadySaved) {
            throw new IllegalStateException("Beneficiary already exists for this user");
        }

        String normalizedBank = bank == null ? "" : bank.trim();
        Beneficiary beneficiary = new Beneficiary(
                0,
                ownerUserId,
                resolvedBeneficiary.user().getUsername(),
                resolvedBeneficiary.account().getAccountNumber(),
                normalizedBank
        );
        repository.add(beneficiary);
        return beneficiary;
    }

    public void add(Beneficiary beneficiary) {
        repository.add(beneficiary);
    }

    public List<Beneficiary> list(long userId) {
        return repository.findByUserId(userId);
    }

    private void ensureLookupDependencies() {
        if (userRepository == null || accountRepository == null) {
            throw new IllegalStateException("Beneficiary lookup services are not configured");
        }
    }

    private ResolvedBeneficiary resolveBeneficiary(String identifier) {
        if (identifier.matches("\\d{10}")) {
            Account account = accountRepository.findByPhoneNumber(identifier)
                    .orElseThrow(() -> new IllegalStateException("No user account found for phone number " + identifier));
            User user = userRepository.findById(account.getUserId())
                    .orElseThrow(() -> new IllegalStateException("No user found for the linked account"));
            return new ResolvedBeneficiary(user, account);
        }

        long userId;
        try {
            userId = Long.parseLong(identifier);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid 10-digit phone number or numeric user ID");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User ID not found: " + userId));
        Account account = accountRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User ID " + userId + " does not have a linked account"));
        return new ResolvedBeneficiary(user, account);
    }

    private record ResolvedBeneficiary(User user, Account account) {
    }
}
