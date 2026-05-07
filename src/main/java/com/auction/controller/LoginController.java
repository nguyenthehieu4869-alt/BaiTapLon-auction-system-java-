package com.auction.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.auction.service.UserDAO;


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
            showError("Vui lòng nhập đầy đủ!");
            return;
        }

        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 kí tự!");
            return;
        }

        UserDAO userDAO = new UserDAO();

        if (userDAO.checkLogin(user, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/role_selection.fxml"));

                Parent root = loader.load();

                RoleSelectionController controller = loader.getController();
                controller.setUsername(user);


                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 700));
                stage.setMaximized(true);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            showError("Sai tài khoản hoặc mật khẩu");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

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
