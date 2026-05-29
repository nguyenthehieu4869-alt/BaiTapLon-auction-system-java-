package com.auction.service.remote;

import com.auction.network.AuctionNetworkClient;
import org.example.network.dto.LoginResponse;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.RegisterRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

public class RemoteUserService {
    public AuthResult login(String username, String password) {
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.LOGIN, new LoginRequest(username, password), true)
        );

        return toAuthResult(response, "Sai tài khoản hoặc mật khẩu!");
    }

    public boolean checkLogin(String username, String password) {
        return login(username, password).isSuccess();
    }

    public AuthResult registerAccount(String username, String email, String password) {
        return registerAccount(username, email, password, "BIDDER");
    }

    public AuthResult registerAccount(String username, String email, String password, String role) {
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.REGISTER, new RegisterRequest(username, email, password, role), true)
        );

        return toAuthResult(response, "Username hoặc email đã tồn tại!");
    }

    public boolean register(String username, String email, String password) {
        return registerAccount(username, email, password).isSuccess();
    }

    private AuthResult toAuthResult(Message response, String fallbackMessage) {
        if (response == null) {
            return new AuthResult(false, "Không nhận được phản hồi từ server!");
        }

        String message = response.getMessage();
        if (message == null || message.isBlank()) {
            message = fallbackMessage;
        }

        String role = null;
        if (response.isSuccess() && response.getData() != null) {
            LoginResponse loginResponse = Protocol.gson().fromJson(
                    Protocol.gson().toJson(response.getData()),
                    LoginResponse.class
            );
            role = loginResponse == null ? null : loginResponse.getRole();
        }

        return new AuthResult(response.isSuccess(), message, role);
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String role;

        public AuthResult(boolean success, String message) {
            this(success, message, null);
        }

        public AuthResult(boolean success, String message, String role) {
            this.success = success;
            this.message = message;
            this.role = role;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getRole() {
            return role;
        }
    }
}
