package org.example.service;

import org.example.common.UserRole;

import java.util.Set;

public final class AccountAuthorization {
    public static final String ADMIN_AUTHORIZATION_ERROR = "bạn không được uỷ quyền là admin";

    private static final Set<String> AUTHORIZED_ADMIN_USERNAMES = Set.of(
            "Huyadmin",
            "Hieuadmin",
            "Kienadmin"
    );
    private static final String AUTHORIZED_ADMIN_PASSWORD = "12345654321";

    private AccountAuthorization() {
    }

    public static void validateRegistration(String username, String password, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Vui lòng chọn role");
        }

        if (role == UserRole.ADMIN && !isAuthorizedAdmin(username, password)) {
            throw new IllegalArgumentException(ADMIN_AUTHORIZATION_ERROR);
        }
    }

    public static boolean isAuthorizedAdmin(String username, String password) {
        return AUTHORIZED_ADMIN_PASSWORD.equals(password)
                && AUTHORIZED_ADMIN_USERNAMES.contains(username);
    }
}
