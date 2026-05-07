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
    private Label welcomeLabel;

    private String username;

    private final ProductDAO productDAO = new ProductDAO();

    private final DecimalFormat priceFormat = new DecimalFormat("#,### VND");

    @FXML
    private void initialize() {
        setupTableColumns();
        loadProducts();
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome Seller: " + username);
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
                new SimpleStringProperty(cellData.getValue().getStatus())
        );
    }

    private void loadProducts() {
        ObservableList<Product> products = productDAO.getAllProducts();
        productTable.setItems(products);
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

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(root, 500, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở form thêm sản phẩm");
        }
    }

    @FXML
    private void handleDeleteProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null){
            showError("Vui lòng chọn sản phẩm cần xoá!");
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
                showInfo("Xoá sản phẩm thành công !");
                loadProducts();
            }else {
                showInfo("Xoá sản phẩm thất bại !");
            }
        }
    }

    @FXML
    private void handleEditProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null){
            showError("Vui lòng chọn sản phẩm cần sửa !");
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
            stage.setScene(new Scene(root, 500, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProducts();

        }catch (IOException e){
            e.printStackTrace();
            showError("Không thể mở trang edit sản phẩm !");
        }
    }

    @FXML
    private void handleCloseAuction() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần đóng đấu giá");
            return;
        }

        if ("CLOSED".equalsIgnoreCase(selectedProduct.getStatus())) {
            showError("Sản phẩm đã được đóng đấu giá !");
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
                showInfo("Đóng đấu giá thành công");
                loadProducts();
            } else {
                showError("Đóng đấu giá thất bại");
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
            showError("Không thể quay về màn hình đăng nhập !");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}