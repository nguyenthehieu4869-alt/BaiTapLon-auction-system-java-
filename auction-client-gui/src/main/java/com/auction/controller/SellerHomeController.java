package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.common.ProductStatus;

import java.text.DecimalFormat;
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

    private String username;

    private final RemoteProductService productService = new RemoteProductService();

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

        ObservableList<Product> products = productService.getProductsBySeller(username);
        productTable.setItems(products);
    }

    private String formatTimeLeft(Product product) {
        if (product.getEndTime() == null) {
            return "N/A";
        }

        LocalDateTime now = LocalDateTime.now();

        if (product.getStartTime() != null && product.getStartTime().isAfter(now)) {
            java.time.Duration duration =
                    java.time.Duration.between(now, product.getStartTime());

            return String.format("Bắt đầu sau %02d:%02d:%02d",
                    duration.toHours(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart());
        }

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
        return ProductStatus.current(product.getStartTime(), product.getEndTime(), product.getStatus());
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
    private void handleViewProfile() {
        openProfile(ProfileController.ProfileMode.SELLER, "Seller Profile");
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/add_product.fxml");
            Parent root = loader.load();

            AddProductController controller = loader.getController();
            controller.setSellerUsername(username);

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(root, 720, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở form thêm sản phẩm");
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
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/edit_product.fxml");

            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProduct(selectedProduct);

            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.setScene(new Scene(root, 720, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        }catch (Exception e){
            e.printStackTrace();
            AlertUtil.showError("Không thể mở trang edit sản phẩm !");
        }
    }

    private void openProfile(ProfileController.ProfileMode mode, String title) {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/profile.fxml");
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 500, 430));
            stage.initModality(Modality.APPLICATION_MODAL);

            if (productTable != null && productTable.getScene() != null) {
                stage.initOwner(productTable.getScene().getWindow());
            }

            ProfileController controller = loader.getController();
            if (!controller.loadProfile(username, mode)) {
                return;
            }

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở màn hình profile");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FxmlUtil.createLoader(getClass(), "/com/auction/view/login.fxml").load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể quay về màn hình đăng nhập !");
        }
    }



}
