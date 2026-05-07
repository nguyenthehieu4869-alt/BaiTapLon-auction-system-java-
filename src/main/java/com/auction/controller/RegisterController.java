package com.auction.controller;

import com.auction.service.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
            showError("Vui lòng nhập username");
            return;
        }

        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            return;
        }

        if (!email.contains("@")) {
            showError("Email không hợp lệ");
            return;
        }

        if (password.isEmpty()) {
            showError("Vui lòng nhập password");
            return;
        }

        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        UserDAO userDAO = new UserDAO();

        boolean success = userDAO.register(username, email, password);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Đăng ký thành công!");
            alert.showAndWait();

            switchToLogin();
        } else {
            showError("Username hoặc email đã tồn tại!");
        }
    }

    @FXML
    private void handleBack() {
        switchToLogin();
    }

    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 650));
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}