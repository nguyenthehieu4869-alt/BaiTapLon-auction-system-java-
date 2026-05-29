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
    private String role;

    private static final String HOME_FXML = "/com/auction/view/home.fxml";
    private static final String SELLER_HOME_FXML = "/com/auction/view/seller_home.fxml";
    private static final String ADMIN_HOME_FXML = "/com/auction/view/admin_home.fxml";

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

        updateAvailableRoles();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role);
        updateAvailableRoles();
    }

    @FXML
    private void handleBidder(ActionEvent event) {
        if (!hasRole("BIDDER")) {
            AlertUtil.showError("Tai khoan nay khong co quyen BIDDER!");
            return;
        }

        switchScene(event, HOME_FXML);
    }

    @FXML
    private void handleSeller(ActionEvent event) {
        if (!hasRole("SELLER")) {
            AlertUtil.showError("Tai khoan nay khong co quyen SELLER!");
            return;
        }

        switchScene(event, SELLER_HOME_FXML);
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        if (!hasRole("ADMIN")) {
            AlertUtil.showError("Tai khoan nay khong co quyen ADMIN!");
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
            AlertUtil.showError("Khong the mo man hinh: " + fxmlPath);
        }
    }

    private boolean hasRole(String expectedRole) {
        return expectedRole.equals(normalizeRole(role));
    }

    private String normalizeRole(String role) {
        return role == null || role.isBlank() ? "BIDDER" : role.trim().toUpperCase();
    }

    private void updateAvailableRoles() {
        if (bidderButton == null || sellerButton == null || adminButton == null) {
            return;
        }

        String normalizedRole = normalizeRole(role);
        bidderButton.setDisable(!"BIDDER".equals(normalizedRole));
        sellerButton.setDisable(!"SELLER".equals(normalizedRole));
        adminButton.setDisable(!"ADMIN".equals(normalizedRole));
    }
}
