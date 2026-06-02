package com.auction.controller;

import com.auction.service.remote.RemoteUserService;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.auction.common.UserRole;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<UserRole> roleComboBox;

    @FXML
    private void initialize() {
        roleComboBox.getItems().setAll(UserRole.BIDDER, UserRole.SELLER, UserRole.ADMIN);
        roleComboBox.setValue(UserRole.BIDDER);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        UserRole role = roleComboBox.getValue();

        if (username.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập username");
            return;
        }

        if (email.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập email");
            return;
        }

        if (!email.contains("@")) {
            AlertUtil.showError("Email không hợp lệ");
            return;
        }

        if (password.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập password");
            return;
        }

        if (password.length() < 6) {
            AlertUtil.showError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (role == null) {
            AlertUtil.showError("Vui lòng chọn role");
            return;
        }

        RemoteUserService userService = new RemoteUserService();
        RemoteUserService.AuthResult registerResult =
                userService.registerAccount(username, email, password, role);

        if (registerResult.isSuccess()) {
            AlertUtil.showInfo("Đăng ký thành công!");
            switchToLogin();
        } else {
            AlertUtil.showError(registerResult.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        switchToLogin();
    }

    private void switchToLogin() {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/login.fxml");
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
