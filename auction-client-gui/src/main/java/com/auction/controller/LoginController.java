package com.auction.controller;

import com.auction.service.remote.RemoteUserService;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String password = passwordField.getText();

        if (user.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Vui long nhap day du!");
            return;
        }

        if (password.length() < 6) {
            AlertUtil.showError("Mat khau phai co it nhat 6 ky tu!");
            return;
        }

        RemoteUserService userService = new RemoteUserService();
        RemoteUserService.AuthResult loginResult = userService.login(user, password);

        if (!loginResult.isSuccess()) {
            AlertUtil.showError(loginResult.getMessage());
            return;
        }

        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/role_selection.fxml");
            Parent root = loader.load();

            RoleSelectionController controller = loader.getController();
            controller.setUsername(user);
            controller.setPassword(password);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Khong the mo man hinh chon vai tro: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/register.fxml");
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Khong the mo man hinh dang ky: " + e.getMessage());
        }
    }
}
