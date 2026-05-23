package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import com.auction.util.PriceFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AdminHomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> idColumn;

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

    private final RemoteProductService productService = new RemoteProductService();
    private Timeline countdownTimeline;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadProducts();
        startCountdownTimer();
    }

    public void setUsername(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome Admin: " + username);
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        startPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getStartPrice()).asObject());
        startPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : PriceFormatter.formatVND(price));
            }
        });

        currentPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCurrentPrice()).asObject());
        currentPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : PriceFormatter.formatVND(price));
            }
        });

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getDisplayStatus(cellData.getValue())));
        timeLeftColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatTimeLeft(cellData.getValue())));
    }

    private void loadProducts() {
        productTable.setItems(productService.getAllProducts());
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    @FXML
    private void handleDeleteFinishedProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            AlertUtil.showError("Vui lòng chỉ chọn sản phẩm đã FINISHED");
            return;
        }

        if (!isFinishedProduct(selectedProduct)) {
            AlertUtil.showWarning("Chỉ có thể xoá sản phẩm đã FINISHED");
            return;
        }

        if (AlertUtil.showConfirm("Xác nhận xoá", "Bạn có muốn xoá sản phẩm: " + selectedProduct.getName() + "?")) {
            if (productService.deleteProduct(selectedProduct.getId())) {
                AlertUtil.showInfo("Xoá sản phẩm thành công");
                loadProducts();
            } else {
                AlertUtil.showError("Xoá sản phẩm thất bại");
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FxmlUtil.createLoader(getClass(), "/com/auction/view/login.fxml").load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể quay về màn hình đăng nhập");
        }
    }

    private String getDisplayStatus(Product product) {
        if (isFinishedProduct(product)) {
            return "FINISHED";
        }
        return product.getStatus();
    }

    private String formatTimeLeft(Product product) {
        if (product.getEndTime() == null) {
            return "N/A";
        }

        if (!product.getEndTime().isAfter(LocalDateTime.now())) {
            return "Ended";
        }

        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), product.getEndTime());
        return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private boolean isFinishedProduct(Product product) {
        return product != null && product.getEndTime() != null && !product.getEndTime().isAfter(LocalDateTime.now());
    }

    private void startCountdownTimer() {
        countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> productTable.refresh()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
}
