package com.auction.controller;

import com.auction.service.remote.RemoteUserService;
import com.auction.util.AlertUtil;
import com.auction.util.PriceFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import com.auction.common.network.dto.UserProfileDTO;

public class ProfileController {
    public enum ProfileMode {
        BIDDER,
        SELLER
    }

    @FXML private Label titleLabel;
    @FXML private Label roleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label countTitleLabel;
    @FXML private Label countValueLabel;
    @FXML private Label walletTitleLabel;
    @FXML private Label walletValueLabel;
    @FXML private Button depositButton;

    private final RemoteUserService userService = new RemoteUserService();
    private ProfileMode currentMode;
    private String currentUsername;

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

        currentUsername = username;
        currentMode = mode;
        renderProfile(result.getProfile(), mode);
        return true;
    }

    @FXML
    private void handleDeposit() {
        if (currentMode != ProfileMode.BIDDER) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền vào ví");
        dialog.setHeaderText("Nhập số tiền muốn nạp");
        dialog.setContentText("Số tiền:");
        dialog.initOwner(getOwnerStage());

        dialog.showAndWait().ifPresent(value -> {
            try {
                double amount = Double.parseDouble(value.trim());
                if (amount <= 0) {
                    AlertUtil.showError("Số tiền nạp phải lớn hơn 0");
                    return;
                }
                RemoteUserService.WalletResult result = userService.depositWallet(amount);
                if (!result.isSuccess()) {
                    AlertUtil.showError(result.getMessage());
                    return;
                }
                AlertUtil.showInfo(result.getMessage());
                refreshProfile();
            } catch (NumberFormatException e) {
                AlertUtil.showError("Vui lòng nhập số hợp lệ");
            }
        });
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void refreshProfile() {
        RemoteUserService.ProfileResult result = userService.getUserProfileResult(currentUsername);
        if (result.isSuccess() && result.getProfile() != null) {
            renderProfile(result.getProfile(), currentMode);
        }
    }

    private void renderProfile(UserProfileDTO profile, ProfileMode mode) {
        boolean seller = mode == ProfileMode.SELLER;

        titleLabel.setText(seller ? "Seller Profile" : "Bidder Profile");
        roleLabel.setText(seller ? "SELLER" : "BIDDER");
        usernameLabel.setText(profile.getUsername());
        emailLabel.setText(profile.getEmail());
        countTitleLabel.setText(seller ? "Số sản phẩm đang bán" : "Số phiên đấu giá đã thắng");
        countValueLabel.setText(String.valueOf(seller ? profile.getSoldProductCount() : profile.getWonAuctionCount()));
        walletTitleLabel.setVisible(!seller);
        walletTitleLabel.setManaged(!seller);
        walletValueLabel.setVisible(!seller);
        walletValueLabel.setManaged(!seller);
        depositButton.setVisible(!seller);
        depositButton.setManaged(!seller);
        if (!seller) {
            walletValueLabel.setText(PriceFormatter.formatVND(profile.getWalletBalance()));
        }
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

    private Stage getOwnerStage() {
        return countValueLabel != null && countValueLabel.getScene() != null
                ? (Stage) countValueLabel.getScene().getWindow()
                : null;
    }
}
