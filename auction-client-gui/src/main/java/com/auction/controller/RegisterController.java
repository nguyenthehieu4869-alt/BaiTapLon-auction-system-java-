package com.auction.controller;

import com.auction.service.UserDAO;
import com.auction.util.FxmlUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.auction.util.AlertUtil;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

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

        UserDAO userDAO = new UserDAO();

        boolean success = userDAO.register(username, email, password);

        if (success) {
            AlertUtil.showInfo("Đăng ký thành công!");
            switchToLogin();
        } else {
            AlertUtil.showError("Username hoặc email đã tồn tại!");
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
