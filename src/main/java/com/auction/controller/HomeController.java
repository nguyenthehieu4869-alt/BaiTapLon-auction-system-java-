package com.auction.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.auction.model.Product;
import com.auction.service.ProductDAO;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.IOException;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    public void setUser(String username) {
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
                    setText(formatPrice(price));
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
                    setText(formatPrice(price));
                }
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        ProductDAO productDAO = new ProductDAO();
        productTable.setItems(productDAO.getAllProducts());
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedProduct) -> {
            if (selectedProduct != null) {
                System.out.println("Đã chọn: " + selectedProduct.getName());
            }
        });
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
            showError("Vui lòng chọn sản phẩm trước");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/view/ProductDetail.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(selectedProduct);

            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở trang chi tiết sản phẩm");
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        return formatter.format(price) + " VND";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}



