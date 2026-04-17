package com.onlinebanking.model;

public class Manager {
    private final long id;
    private final String username;
    private final String role; // MANAGER

    public Manager(long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
