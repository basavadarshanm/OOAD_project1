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
}
