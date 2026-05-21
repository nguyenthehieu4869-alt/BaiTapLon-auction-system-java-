package com.auction.controller;

import com.auction.service.BidResult;
import com.auction.service.BidService;
import com.auction.model.Product;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.auction.model.Bid;
import com.auction.service.BidDAO;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import com.auction.util.PriceFormatter;

import java.io.File;
import java.time.LocalDateTime;

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
    private ImageView productImageView;

    @FXML
    private Label imagePlaceholderLabel;

    @FXML
    private TextField bidAmountField;

    @FXML
    private TableView<Bid> bidHistoryTable;

    @FXML
    private TableColumn<Bid, String> bidderUsernameColumn;

    @FXML
    private TableColumn<Bid, Double> bidPriceColumn;

    @FXML
    private TableColumn<Bid, java.sql.Timestamp> bidTimeColumn;

    @FXML
    private Button placeBidButton;

    private Product product;

    private final BidService bidService = new BidService();

    private final BidDAO bidDAO = new BidDAO();

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handlePlaceBid() {
        try {
            double newPrice = Double.parseDouble(bidAmountField.getText());

            if (isAuctionEnded()) {
                AlertUtil.showError("Phiên đấu giá đã kết thúc, không thể đặt giá.");
                return;
            }

            BidResult result = bidService.placeBid(product, username, newPrice);

            if (!result.isSuccess()) {
                AlertUtil.showError(result.getMessage());
                return;
            }

            product.setCurrentPrice(newPrice);
            currentPriceLabel.setText(PriceFormatter.formatVND(newPrice));

            loadBidHistory();

            AlertUtil.showInfo(result.getMessage());

        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui lòng nhập giá hợp lệ");
        }

    }

    @FXML
    private void handleBack(){
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/home.fxml");

            Parent root=loader.load();

            HomeController controller = loader.getController();
            controller.setUser(username);

            Stage stage=(Stage) productNameLabel.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể quay lại trang chủ");
        }
    }


    public void setProduct(Product product) {
        this.product = product;

        productNameLabel.setText("Name: " + product.getName());
        descriptionLabel.setText("Description: " + product.getDescription());
        currentPriceLabel.setText(PriceFormatter.formatVND(product.getCurrentPrice()));
        statusLabel.setText("Status: " + product.getStatus());
        updateProductImage(product.getImagePath());

        bidderUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("bidderUsername"));
        bidPriceColumn.setCellValueFactory(new PropertyValueFactory<>("bidPrice"));

        bidPriceColumn.setCellFactory(column -> new TableCell<Bid, Double>() {
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

        bidTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bidTime"));

        loadBidHistory();
        updateBidControls();
    }

    private void loadBidHistory(){
        if (product == null) {
            return;
        }
        bidHistoryTable.setItems(bidDAO.getBidsByProductId(product.getId()));
    }

    private void updateBidControls() {
        boolean ended = isAuctionEnded();
        bidAmountField.setDisable(ended);
        placeBidButton.setDisable(ended);
    }

    private boolean isAuctionEnded() {
        if (product == null) {
            return true;
        }

        if ("CLOSED".equalsIgnoreCase(product.getStatus())
                || "FINISHED".equalsIgnoreCase(product.getStatus())
                || "CANCELED".equalsIgnoreCase(product.getStatus())) {
            return true;
        }

        return product.getEndTime() != null
                && !product.getEndTime().isAfter(LocalDateTime.now());
    }

    private void updateProductImage(String imagePath) {
        if (productImageView == null || imagePlaceholderLabel == null) {
            return;
        }

        if (imagePath == null || imagePath.isBlank()) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setText("No product image");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setText("Image file not found");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = new Image(imageFile.toURI().toString(), 208, 168, true, true);

            if (image.isError()) {
                productImageView.setImage(null);
                imagePlaceholderLabel.setText("Unable to load image");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImageView.setImage(image);
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setText("Unable to load image");
            imagePlaceholderLabel.setVisible(true);
        }
    }
}
