package com.auction.service.remote;

import com.auction.network.AuctionNetworkClient;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.RegisterRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;

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
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.REGISTER, new RegisterRequest(username, email, password), true)
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

        return new AuthResult(response.isSuccess(), message);
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;

        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
