package org.example.model;

public final class UserFactory {
    private UserFactory() {
    }

    public static User create(int id, String username, String password, String email, String role) {
        return switch (UserRole.from(role)) {
            case ADMIN -> new Admin(id, username, password, email);
            case SELLER -> new Seller(id, username, password, email);
            case BIDDER -> new Bidder(id, username, password, email);
        };
    }
}
