package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import com.auction.model.Product;
import com.auction.service.ProductDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;

import javafx.stage.Modality;
import javafx.stage.Stage;
import com.auction.util.AlertUtil;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.time.LocalDateTime;


public class SellerHomeController {

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

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField durationMinutesField;

    private String username;

    private final ProductDAO productDAO = new ProductDAO();

    private final DecimalFormat priceFormat = new DecimalFormat("#,### VND");

    private Timeline countdownTimeline;

    @FXML
    private void initialize() {
        setupTableColumns();
        startCountdownTimer();
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome Seller: " + username);
        loadProducts();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject()
        );

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName())
        );

        startPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getStartPrice()).asObject());

        startPriceColumn.setCellFactory(column -> new TableCell<Product,Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price,empty);

                if (empty || price == null){
                    setText(null);
                }else {
                    setText(priceFormat.format(price));
                }
            }
        });

        currentPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCurrentPrice()).asObject());

        currentPriceColumn.setCellFactory(column -> new TableCell<Product,Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price,empty);

                if (empty || price == null){
                    setText(null);
                }else {
                    setText(priceFormat.format(price));
                }
            }
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getDisplayStatus(cellData.getValue()))
        );

        timeLeftColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTimeLeft(cellData.getValue()))
        );
    }

    private void loadProducts() {
        if (username == null || username.isBlank()) {
            productTable.setItems(javafx.collections.FXCollections.observableArrayList());
            return;
        }

        ObservableList<Product> products = productDAO.getProductsBySeller(username);
        productTable.setItems(products);
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

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/add_product.fxml"));
            Parent root = loader.load();

            AddProductController controller = loader.getController();
            controller.setSellerUsername(username);

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(root, 720, 740));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở form thêm sản phẩm");
        }
    }

    @FXML
    private void handleDeleteProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null){
            AlertUtil.showError("Vui lòng chọn sản phẩm cần xoá!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xoá");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có muốn xoá: " + selectedProduct.getName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            boolean success = productDAO.deleteProduct(selectedProduct.getId());

            if (success){
                AlertUtil.showInfo("Xoá sản phẩm thành công !");
                loadProducts();
            }else {
                AlertUtil.showInfo("Xoá sản phẩm thất bại !");
            }
        }
    }

    @FXML
    private void handleEditProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null){
            AlertUtil.showError("Vui lòng chọn sản phẩm cần sửa !");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().
                    getResource("/com/auction/view/edit_product.fxml"));

            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProduct(selectedProduct);

            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.setScene(new Scene(root, 720, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        }catch (IOException e){
            e.printStackTrace();
            AlertUtil.showError("Không thể mở trang edit sản phẩm !");
        }
    }

    @FXML
    private void handleCloseAuction() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            AlertUtil.showError("Vui lòng chọn sản phẩm cần đóng đấu giá");
            return;
        }

        if ("CLOSED".equalsIgnoreCase(selectedProduct.getStatus())) {
            AlertUtil.showInfo("Sản phẩm đã được đóng đấu giá !");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đóng đấu giá");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc muốn đóng đấu giá sản phẩm: " + selectedProduct.getName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = productDAO.closeAuction(selectedProduct.getId());

            if (success) {
                AlertUtil.showInfo("Đóng đấu giá thành công");
                loadProducts();
            } else {
                AlertUtil.showError("Đóng đấu giá thất bại");
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/auction/view/login.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể quay về màn hình đăng nhập !");
        }
    }



}
