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
import com.auction.common.UserRole;

public class LoginController {
    private static final String BIDDER_HOME_FXML = "/com/auction/view/home.fxml";
    private static final String SELLER_HOME_FXML = "/com/auction/view/seller_home.fxml";
    private static final String ADMIN_HOME_FXML = "/com/auction/view/admin_home.fxml";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập đầy đủ!");
            return;
        }

        if (password.length() < 6) {
            AlertUtil.showError("Mật khẩu phải chứa ít nhất 6 kí tự!");
            return;
        }

        RemoteUserService userService = new RemoteUserService();
        RemoteUserService.AuthResult loginResult = userService.login(username, password);

        if (!loginResult.isSuccess()) {
            AlertUtil.showError(loginResult.getMessage());
            return;
        }

        openHome(username, loginResult.getRole());
    }

    private void openHome(String username, UserRole role) {
        if (role == null) {
            AlertUtil.showError("Không xác định được role của tài khoản.");
            return;
        }

        String fxmlPath = switch (role) {
            case BIDDER -> BIDDER_HOME_FXML;
            case SELLER -> SELLER_HOME_FXML;
            case ADMIN -> ADMIN_HOME_FXML;
        };

        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), fxmlPath);
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof HomeController homeController) {
                homeController.setUser(username);
            } else if (controller instanceof SellerHomeController sellerController) {
                sellerController.setUsername(username);
            } else if (controller instanceof AdminHomeController adminController) {
                adminController.setUsername(username);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở màn hình cho role " + role + ": " + e.getMessage());
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
            AlertUtil.showError("Không thể mở màn hình đăng ký: " + e.getMessage());
        }
    }
}
