package com.onlinebanking.repository;

import java.util.List;
import java.util.Optional;

import com.onlinebanking.model.User;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(long id);
    List<User> findAllByRole(String role);
    List<User> findAll();
    User create(String username, String passwordHash, String role);
    void updatePassword(long userId, String passwordHash);
    void updateMpin(long userId, String mpinHash);
    boolean hasMpin(long userId);
    boolean verifyMpin(long userId, String mpinHash);
    void updateRole(long userId, String role);
    void updateBlockStatus(long userId, boolean isBlocked);
    void deleteUser(long userId);
    boolean isUserBlocked(long userId);
}
