package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.ProductDAO;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;
import com.auction.model.Bid;
import com.auction.service.BidDAO;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;

public class ProductDetailController {

    @FXML
    private Label productNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField bidAmountField;

    @FXML
    private TableView<Bid> bidHistoryTable;

    @FXML
    private TableColumn<Bid, String> bidderNameColumn;

    @FXML
    private TableColumn<Bid, Double> bidAmountColumn;

    @FXML
    private TableColumn<Bid, java.sql.Timestamp> bidTimeColumn;

    @FXML
    private Button placeBidButton;

    private Product product;

    @FXML
    private void handlePlaceBid() {
        try {
            String bidText = bidAmountField.getText();

            if (bidText == null || bidText.trim().isEmpty()) {
                showAlert("Vui lòng nhập mức giá thầu");
                return;
            }

            if ("CLOSED".equalsIgnoreCase(product.getStatus())){
                showAlert("Sản phẩm đã đóng đấu giá !");
                return;
            }

            double newPrice = Double.parseDouble(bidText);

            if (newPrice <= product.getCurrentPrice()) {
                showAlert("Giá thầu phải lớn hơn giá hiện tại");
                return;
            }

            ProductDAO productDAO = new ProductDAO();

            boolean success = productDAO.updateCurrentPrice(product.getId(), newPrice);

            if (success) {
                BidDAO bidDAO = new BidDAO();
                boolean bidSaved = bidDAO.addBid(product.getId(), "Guest", newPrice);

                if (bidSaved) {
                    showAlert("Đặt giá thành công");
                } else {
                    showAlert("Đặt giá thành công nhưng chưa lưu được lịch sử");
                }


                currentPriceLabel.setText(formatPrice(newPrice));

                product = new Product(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getStartPrice(),
                        newPrice,
                        product.getStatus()
                );

            } else {
                showAlert("Đặt giá thất bại");
            }

        } catch (NumberFormatException e) {
            showAlert("Giá thầu phải là số");
        }
    }

    @FXML
    private void handleBack(){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/view/home.fxml")
            );

            Parent root=loader.load();

            Stage stage=(Stage) productNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể quay lại trang chủ");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        return formatter.format(price) + " VND";
    }

    public void setProduct(Product product) {
        this.product = product;

        productNameLabel.setText("Name: " + product.getName());
        descriptionLabel.setText("Description: " + product.getDescription());
        currentPriceLabel.setText(formatPrice(product.getCurrentPrice()));
        statusLabel.setText("Status: " + product.getStatus());

        bidderNameColumn.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        bidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));

        bidAmountColumn.setCellFactory(column -> new TableCell<Bid, Double>() {
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

        bidTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bidTime"));

        loadBidHistory();

        if ("CLOSED".equalsIgnoreCase(product.getStatus())) {
            bidAmountField.setDisable(true);
            placeBidButton.setDisable(true);
        }
    }

    private void loadBidHistory(){
        BidDAO bidDAO = new BidDAO();
        bidHistoryTable.setItems(bidDAO.getBidsByProductId(product.getId()));
    }
}