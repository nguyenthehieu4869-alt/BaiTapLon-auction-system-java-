package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {
    private String username;

    private static final String HOME_FXML = "/com/auction/view/home.fxml";
    private static final String SELLER_HOME_FXML = "/com/auction/view/seller_home.fxml";
    private static final String ADMIN_HOME_FXML = "/com/auction/view/admin_home.fxml";

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handleBidder(ActionEvent event) {
        switchScene(event,HOME_FXML);
    }

    @FXML
    private void handleSeller(ActionEvent event) {
        switchScene(event,SELLER_HOME_FXML);
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        showInfo("Đang phát triển...");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof HomeController) {
                HomeController homeController = (HomeController) controller;
                homeController.setUser(username);
            }

            if (controller instanceof SellerHomeController) {
                SellerHomeController sellerController = (SellerHomeController) controller;
                sellerController.setUsername(username);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở màn hình: " + fxmlPath);
        }
    }



    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}



