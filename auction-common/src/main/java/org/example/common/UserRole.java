package org.example.common;

import java.util.Locale;

public enum UserRole {
    BIDDER("Bidder"),
    SELLER("Seller"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public static UserRole fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User role is required");
        }

        return UserRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
