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
import com.auction.service.BidDAO;
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

    @FXML
    private Label emptyStateLabel;

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
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

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
        productTable.getStyleClass().add("bidder-table");
        productTable.setFixedCellSize(42);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        startPriceColumn.setStyle("-fx-alignment: CENTER_RIGHT;");
        currentPriceColumn.setStyle("-fx-alignment: CENTER_RIGHT;");
        timeLeftColumn.setStyle("-fx-alignment: CENTER;");

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
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getDisplayStatus(cellData.getValue()))
        );
        statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-open", "status-finished", "status-muted");

                if (empty || status == null) {
                    setText(null);
                    return;
                }

                setText(status);

                if (status.startsWith("OPENING") || status.equalsIgnoreCase("OPEN")) {
                    getStyleClass().add("status-open");
                } else if (status.startsWith("FINISHED") || status.equalsIgnoreCase("CLOSED")) {
                    getStyleClass().add("status-finished");
                } else {
                    getStyleClass().add("status-muted");
                }
            }
        });

        timeLeftColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTimeLeft(cellData.getValue()))
        );

        ProductDAO productDAO = new ProductDAO();
        productTable.setItems(productDAO.getAllProducts());
        updateEmptyStateMessage();
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedProduct) -> {

        });

        startCountdownTimer();
    }

    @FXML
    private void handleRefresh() {
        ProductDAO productDAO = new ProductDAO();
        productTable.setItems(productDAO.getAllProducts());
        updateEmptyStateMessage();
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
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
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
        String leaderUsername = new BidDAO().getWinnerUsernameByProductId(product.getId());

        if (product.getEndTime() != null &&
                !product.getEndTime().isAfter(LocalDateTime.now())) {
            return leaderUsername == null || leaderUsername.isBlank()
                    ? "FINISHED"
                    : "FINISHED - Winner: " + leaderUsername;
        }

        return leaderUsername == null || leaderUsername.isBlank()
                ? product.getStatus()
                : "OPENING - Leader: " + leaderUsername;
    }

    private void startCountdownTimer() {
        countdownTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    productTable.refresh();
                    updateEmptyStateMessage();
                })
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateEmptyStateMessage() {
        if (emptyStateLabel == null || productTable == null) {
            return;
        }

        boolean hasAvailableProducts = productTable.getItems() != null
                && productTable.getItems().stream().anyMatch(this::isAuctionAvailable);

        emptyStateLabel.setVisible(!hasAvailableProducts);
        emptyStateLabel.setManaged(!hasAvailableProducts);
    }

    private boolean isAuctionAvailable(Product product) {
        if (product == null) {
            return false;
        }

        if (product.getEndTime() != null && !product.getEndTime().isAfter(LocalDateTime.now())) {
            return false;
        }

        String status = product.getStatus();
        return status != null && !status.equalsIgnoreCase("DELETED") && !status.equalsIgnoreCase("CLOSED");
    }
}



