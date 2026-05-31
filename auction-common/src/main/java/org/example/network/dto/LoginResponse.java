package org.example.network.dto;

import org.example.common.UserRole;

public class LoginResponse {
    private String username;
    private UserRole role;

    public LoginResponse(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
