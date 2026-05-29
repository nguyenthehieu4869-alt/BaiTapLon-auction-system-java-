package org.example.model;

public enum UserRole {
    BIDDER,
    SELLER,
    ADMIN;

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            return BIDDER;
        }

        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BIDDER;
        }
    }
}
