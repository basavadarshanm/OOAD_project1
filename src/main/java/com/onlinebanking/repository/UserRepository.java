package com.onlinebanking.repository;

import java.util.Optional;

import com.onlinebanking.model.User;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(long id);
    User create(String username, String passwordHash, String role);
}
