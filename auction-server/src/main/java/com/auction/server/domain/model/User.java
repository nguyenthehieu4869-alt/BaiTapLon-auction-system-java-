package com.auction.server.domain.model;

import com.auction.server.domain.observer.AuctionObserver;

public abstract class User extends Entity implements AuctionObserver {
    protected String username;
    protected UserRole role;
    public User(String username, UserRole role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (role == null) {
            throw new IllegalArgumentException("User role is required");
        }

        this.username = username.trim();
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}