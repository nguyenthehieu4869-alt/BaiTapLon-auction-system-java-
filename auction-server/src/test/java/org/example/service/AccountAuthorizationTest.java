package org.example.service;

import org.example.common.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountAuthorizationTest {

    @Test
    void allowsRegularBidderAndSellerRegistrations() {
        assertDoesNotThrow(() ->
                AccountAuthorization.validateRegistration("bidder", "123456", UserRole.BIDDER));
        assertDoesNotThrow(() ->
                AccountAuthorization.validateRegistration("seller", "123456", UserRole.SELLER));
    }

    @Test
    void allowsOnlyConfiguredAdminAccounts() {
        assertDoesNotThrow(() ->
                AccountAuthorization.validateRegistration("Huyadmin", "12345654321", UserRole.ADMIN));
        assertDoesNotThrow(() ->
                AccountAuthorization.validateRegistration("Hieuadmin", "12345654321", UserRole.ADMIN));
        assertDoesNotThrow(() ->
                AccountAuthorization.validateRegistration("Kienadmin", "12345654321", UserRole.ADMIN));
    }

    @Test
    void rejectsUnauthorizedAdminRegistration() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AccountAuthorization.validateRegistration("user", "123456", UserRole.ADMIN)
        );

        assertEquals(AccountAuthorization.ADMIN_AUTHORIZATION_ERROR, exception.getMessage());
    }
}
