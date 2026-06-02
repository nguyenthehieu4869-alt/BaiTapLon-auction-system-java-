package com.auction.common.network.dto;

import com.auction.common.UserRole;

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
