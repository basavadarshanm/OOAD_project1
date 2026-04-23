package com.onlinebanking.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.onlinebanking.dto.UserManagementDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.TransactionRepository;
import com.onlinebanking.repository.UserRepository;

/**
 * Service Layer Pattern: manager-only business operations for users and transactions.
 */
public class ManagerService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ManagerService(UserRepository userRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get all customers
     */
    public List<User> getAllCustomers() {
        return userRepository.findAllByRole("CUSTOMER");
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserManagementDto> getAllUserSummaries() {
        // DTO Pattern: isolate UI from direct domain entity exposure.
        return userRepository.findAll().stream()
                .map(u -> new UserManagementDto(u.getId(), u.getUsername(), u.getRole(), userRepository.isUserBlocked(u.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific user
     */
    public Optional<User> getUser(long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Block a user account
     */
    public void blockUser(long userId) {
        blockUser(String.valueOf(userId));
    }

    public void blockUser(String userIdOrAccountNumber) {
        long userId = resolveUserId(userIdOrAccountNumber);
        userRepository.updateBlockStatus(userId, true);
    }

    /**
     * Unblock a user account
     */
    public void unblockUser(long userId) {
        unblockUser(String.valueOf(userId));
    }

    public void unblockUser(String userIdOrAccountNumber) {
        long userId = resolveUserId(userIdOrAccountNumber);
        userRepository.updateBlockStatus(userId, false);
    }

    /**
     * Check if user is blocked
     */
    public boolean isUserBlocked(long userId) {
        return userRepository.isUserBlocked(userId);
    }

    /**
     * Delete a user
     */
    public void deleteUser(long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("User not found");
        }
        if ("MANAGER".equalsIgnoreCase(userOpt.get().getRole())) {
            throw new IllegalStateException("Manager users cannot be deleted");
        }
        userRepository.deleteUser(userId);
    }

    public void updateUserRole(long userId, String role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("User not found");
        }
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        if (!"CUSTOMER".equals(normalizedRole) && !"MANAGER".equals(normalizedRole)) {
            throw new IllegalArgumentException("Role must be CUSTOMER or MANAGER");
        }
        userRepository.updateRole(userId, normalizedRole);
    }

    /**
     * Create a new user (by admin)
     */
    public User createUser(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        return userRepository.create(username, password, role);
    }

    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Get transaction by ID
     */
    public Optional<Transaction> getTransaction(long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    private long resolveUserId(String userIdOrAccountNumber) {
        String normalized = userIdOrAccountNumber == null ? "" : userIdOrAccountNumber.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("User ID or account number is required");
        }

        try {
            long userId = Long.parseLong(normalized);
            if (userRepository.findById(userId).isPresent()) {
                return userId;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to account-number lookup.
        }

        Optional<Account> accountOpt = accountRepository.findByAccountNumber(normalized);
        if (accountOpt.isPresent()) {
            return accountOpt.get().getUserId();
        }

        throw new IllegalStateException("User not found for ID or account number: " + normalized);
    }
}
