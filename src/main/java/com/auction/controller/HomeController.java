package com.auction.controller;

import com.auction.util.AlertUtil;
import com.auction.util.PriceFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.auction.model.Product;
import com.auction.service.ProductDAO;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;

import java.io.IOException;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    private String username;

    public void setUser(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username);
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/auction/view/login.fxml")
            );

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TableView<Product> productTable;


    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, Double> startPriceColumn;

    @FXML
    private TableColumn<Product, Double> currentPriceColumn;

    @FXML
    private TableColumn<Product, String> statusColumn;

    @FXML
    private TableColumn<Product, String> timeLeftColumn;

    private Timeline countdownTimeline;

    @FXML
    public void initialize() {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        startPriceColumn.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        startPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(PriceFormatter.formatVND(price));
                }
            }
        });

        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        currentPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(PriceFormatter.formatVND(price));
                }
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getDisplayStatus(cellData.getValue()))
        );

        timeLeftColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTimeLeft(cellData.getValue()))
        );

        ProductDAO productDAO = new ProductDAO();
        productTable.setItems(productDAO.getAllProducts());
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedProduct) -> {
            if (selectedProduct != null) {
                System.out.println("Đã chọn: " + selectedProduct.getName());
            }
        });

        startCountdownTimer();
    }

    @FXML
    private void handleRefresh() {
        ProductDAO productDAO = new ProductDAO();
        productTable.setItems(productDAO.getAllProducts());
    }

    @FXML
    private void handleViewDetail() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            AlertUtil.showError("Vui lòng chọn sản phẩm trước");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/ProductDetail.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(selectedProduct);
            controller.setUsername(username);


            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở trang chi tiết sản phẩm");
        }
    }

    private String formatTimeLeft(Product product) {
        if (product.getEndTime() == null) {
            return "N/A";
        }

        LocalDateTime now = LocalDateTime.now();

        if (!product.getEndTime().isAfter(now)) {
            return "Ended";
        }

        java.time.Duration duration =
                java.time.Duration.between(now, product.getEndTime());

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getDisplayStatus(Product product) {
        if (product.getEndTime() != null &&
                !product.getEndTime().isAfter(LocalDateTime.now())) {
            return "FINISHED";
        }

        return product.getStatus();
    }

    private void startCountdownTimer() {
        countdownTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    productTable.refresh();
                })
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
}



