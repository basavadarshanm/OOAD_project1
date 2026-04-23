package com.onlinebanking.model;

public class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final String mpinHash;
    private final String role; // CUSTOMER or ADMIN

    public User(long id, String username, String passwordHash, String role) {
        this(id, username, passwordHash, null, role);
    }

    public User(long id, String username, String passwordHash, String mpinHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.mpinHash = mpinHash;
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

    public String getMpinHash() {
        return mpinHash;
    }

    public String getRole() {
        return role;
    }
}
