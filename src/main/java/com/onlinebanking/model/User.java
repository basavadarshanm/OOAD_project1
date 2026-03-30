package com.onlinebanking.model;

public class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final String role; // CUSTOMER or ADMIN

    public User(long id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}
