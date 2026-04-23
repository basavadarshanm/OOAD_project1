package com.onlinebanking.service;

import java.util.Optional;

import com.onlinebanking.model.User;
import com.onlinebanking.repository.UserRepository;

/**
 * Handles authentication. Replace the plain-text check with a secure hash in production.
 */
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String passwordPlain) {
        return userRepository.findByUsername(username)
                .filter(user -> !userRepository.isUserBlocked(user.getId()))
                .filter(user -> user.getPasswordHash().equals(passwordPlain));
    }

    public User register(String username, String passwordPlain) {
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (passwordPlain == null || passwordPlain.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        // In production, store a secure password hash instead of plain text.
        return userRepository.create(normalizedUsername, passwordPlain, "CUSTOMER");
    }

    public void changePassword(long userId, String currentPasswordPlain, String newPasswordPlain) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (currentPasswordPlain == null || !user.getPasswordHash().equals(currentPasswordPlain)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPasswordPlain == null || newPasswordPlain.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (newPasswordPlain.equals(currentPasswordPlain)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        userRepository.updatePassword(userId, newPasswordPlain);
    }

    public void setMpin(long userId, String mpinPlain) {
        if (mpinPlain == null || !mpinPlain.matches("\\d{4}")) {
            throw new IllegalArgumentException("MPIN must be exactly 4 digits");
        }
        userRepository.updateMpin(userId, mpinPlain);
    }

    public boolean hasMpin(long userId) {
        return userRepository.hasMpin(userId);
    }

    public boolean verifyMpin(long userId, String mpinPlain) {
        if (mpinPlain == null || !mpinPlain.matches("\\d{4}")) {
            return false;
        }
        return userRepository.verifyMpin(userId, mpinPlain);
    }
}
