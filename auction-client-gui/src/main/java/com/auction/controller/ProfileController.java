package com.auction.controller;

import com.auction.service.remote.RemoteUserService;
import com.auction.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.network.dto.UserProfileDTO;

public class ProfileController {
    public enum ProfileMode {
        BIDDER,
        SELLER
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label countTitleLabel;

    @FXML
    private Label countValueLabel;

    private final RemoteUserService userService = new RemoteUserService();

    public boolean loadProfile(String username, ProfileMode mode) {
        if (username == null || username.isBlank()) {
            AlertUtil.showError("Không xác định được tài khoản!");
            return false;
        }

        RemoteUserService.ProfileResult result = userService.getUserProfileResult(username);
        if (!result.isSuccess() || result.getProfile() == null) {
            AlertUtil.showError(result.getMessage());
            return false;
        }

        renderProfile(result.getProfile(), mode);
        return true;
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void renderProfile(UserProfileDTO profile, ProfileMode mode) {
        boolean seller = mode == ProfileMode.SELLER;

        titleLabel.setText(seller ? "Seller Profile" : "Bidder Profile");
        roleLabel.setText(seller ? "SELLER" : "BIDDER");
        usernameLabel.setText(profile.getUsername());
        emailLabel.setText(profile.getEmail());
        countTitleLabel.setText(seller ? "Số sản phẩm đang bán" : "Số phiên đấu giá đã thắng");
        countValueLabel.setText(String.valueOf(
                seller ? profile.getSoldProductCount() : profile.getWonAuctionCount()
        ));
    }

    private void closeWindow() {
        if (countValueLabel == null || countValueLabel.getScene() == null) {
            return;
        }

        Stage stage = (Stage) countValueLabel.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
