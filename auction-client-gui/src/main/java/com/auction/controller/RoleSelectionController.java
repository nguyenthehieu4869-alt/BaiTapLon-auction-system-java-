package com.auction.controller;

import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class RoleSelectionController {
    @FXML
    private Button bidderButton;

    @FXML
    private Button sellerButton;

    @FXML
    private Button adminButton;

    private String username;
    private String password;

    private static final String HOME_FXML = "/com/auction/view/home.fxml";
    private static final String SELLER_HOME_FXML = "/com/auction/view/seller_home.fxml";
    private static final String ADMIN_HOME_FXML = "/com/auction/view/admin_home.fxml";
    private static final String AUTHORIZED_ADMIN_USERNAME = "huy";
    private static final String AUTHORIZED_ADMIN_USERNAME_2 = "hieu";
    private static final String AUTHORIZED_ADMIN_USERNAME_3 = "kien";
    private static final String AUTHORIZED_ADMIN_PASSWORD = "123456";

    @FXML
    private void initialize() {
        if (bidderButton != null) {
            bidderButton.setOnAction(this::handleBidder);
        }

        if (sellerButton != null) {
            sellerButton.setOnAction(this::handleSeller);
        }

        if (adminButton != null) {
            adminButton.setOnAction(this::handleAdmin);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @FXML
    private void handleBidder(ActionEvent event) {
        switchScene(event, HOME_FXML);
    }

    @FXML
    private void handleSeller(ActionEvent event) {
        switchScene(event, SELLER_HOME_FXML);
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        if (!isAuthorizedAdmin()) {
            AlertUtil.showError("Tài khoản này không có quyền ADMIN!");
            return;
        }

        switchScene(event, ADMIN_HOME_FXML);
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), fxmlPath);
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof HomeController homeController) {
                homeController.setUser(username);
            }

            if (controller instanceof SellerHomeController sellerController) {
                sellerController.setUsername(username);
            }

            if (controller instanceof AdminHomeController adminController) {
                adminController.setUsername(username);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở màn hình: " + fxmlPath);
        }
    }

    private boolean isAuthorizedAdmin() {
        return AUTHORIZED_ADMIN_PASSWORD.equals(password)
                && (AUTHORIZED_ADMIN_USERNAME.equals(username)
                || AUTHORIZED_ADMIN_USERNAME_2.equals(username)
                || AUTHORIZED_ADMIN_USERNAME_3.equals(username));
    }
}
