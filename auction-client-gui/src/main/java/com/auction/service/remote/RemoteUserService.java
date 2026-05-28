package com.auction.service.remote;

import com.auction.network.AuctionNetworkClient;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.RegisterRequest;
import org.example.network.dto.UserProfileDTO;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.util.HashMap;
import java.util.Map;

public class RemoteUserService {
    public AuthResult login(String username, String password) {
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.LOGIN, new LoginRequest(username, password), true)
        );

        return toAuthResult(response, "Sai tài khoản hoặc mật khẩu!");
    }

    public AuthResult registerAccount(String username, String email, String password) {
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.REGISTER, new RegisterRequest(username, email, password), true)
        );

        return toAuthResult(response, "Username hoặc email đã tồn tại!");
    }

    public ProfileResult getUserProfileResult(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.GET_USER_PROFILE, data, true)
        );

        if (response == null) {
            return new ProfileResult(false, "Không nhận được phản hồi từ server.", null);
        }

        if (!response.isSuccess()) {
            String message = response.getMessage();
            if (message == null || message.isBlank()) {
                message = "Không tải được thông tin profile.";
            } else if ("Message không hợp lệ".equalsIgnoreCase(message.trim())
                    || "Message khong hop le".equalsIgnoreCase(message.trim())) {
                message = "Server đang chạy bản cũ. Hãy khởi động lại AuctionServer để nạp tính năng profile.";
            }

            return new ProfileResult(false, message, null);
        }

        if (response.getData() == null) {
            return new ProfileResult(false, "Server không trả về dữ liệu profile.", null);
        }

        UserProfileDTO profile = Protocol.gson().fromJson(
                Protocol.gson().toJson(response.getData()),
                UserProfileDTO.class
        );

        return new ProfileResult(true, "Load profile thành công", profile);
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

    public static class ProfileResult {
        private final boolean success;
        private final String message;
        private final UserProfileDTO profile;

        public ProfileResult(boolean success, String message, UserProfileDTO profile) {
            this.success = success;
            this.message = message;
            this.profile = profile;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public UserProfileDTO getProfile() {
            return profile;
        }
    }
}
