package com.auction.controller;

import com.auction.model.Product;

import com.auction.network.AuctionNetworkClient;
import com.auction.network.BidUpdateListener;
import com.auction.service.remote.RemoteBidService;
import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import com.auction.util.FxmlUtil;
import com.auction.util.PriceFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label openingNoticeLabel;

    @FXML
    private Label emptyStateLabel;

    private String username;

    public void setUser(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username);
    }

    @FXML
    private void handleLogout() {
        AuctionNetworkClient.getInstance().removeBidUpdateListener(bidUpdateListener);
        try {
            Parent root = FxmlUtil.createLoader(getClass(), "/com/auction/view/login.fxml").load();

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
    private final RemoteProductService productService = new RemoteProductService();
    private final RemoteBidService bidService = new RemoteBidService();
    private final Map<Integer, String> leaderUsernameCache = new HashMap<>();
    private final Set<Integer> leaderCacheLoaded = new HashSet<>();
    private final BidUpdateListener bidUpdateListener = message -> handleBidUpdate(message);

    @FXML
    public void initialize() {
        productTable.getStyleClass().add("bidder-table");
        productTable.setFixedCellSize(42);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        startPriceColumn.setStyle("-fx-alignment: CENTER_RIGHT;");
        currentPriceColumn.setStyle("-fx-alignment: CENTER_RIGHT;");
        timeLeftColumn.setStyle("-fx-alignment: CENTER;");
        AuctionNetworkClient.getInstance().addBidUpdateListener(bidUpdateListener);

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
                    getStyleClass().add("status-comingsoon");
                }
            }
        });

        timeLeftColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatTimeLeft(cellData.getValue()))
        );

        productTable.setItems(productService.getAllProducts());
        updateDashboardMessages();
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedProduct) -> {

        });

        startCountdownTimer();
    }

    @FXML
    private void handleRefresh() {
        leaderUsernameCache.clear();
        leaderCacheLoaded.clear();
        productTable.setItems(productService.getAllProducts());
        updateDashboardMessages();
    }

    private void handleBidUpdate(org.example.network.protocol.Message message) {
        if (message == null || message.getData() == null || productTable == null) {
            return;
        }

        Object dataObj = message.getData();
        if (!(dataObj instanceof Map<?, ?> data)) {
            return;
        }

        Object productIdObj = data.get("productId");
        Object currentPriceObj = data.get("currentPrice");
        Object endTimeObj = data.get("endTime");
        Object bidderUsernameObj = data.get("bidderUsername");

        if (!(productIdObj instanceof Number productIdNumber) || !(currentPriceObj instanceof Number currentPriceNumber)) {
            return;
        }

        int productId = productIdNumber.intValue();

        for (Product product : productTable.getItems()) {
            if (product.getId() != productId) {
                continue;
            }

            product.setCurrentPrice(currentPriceNumber.doubleValue());

            if (endTimeObj instanceof String endTimeText && !endTimeText.isBlank()) {
                product.setEndTime(LocalDateTime.parse(endTimeText));
            }

            if (bidderUsernameObj instanceof String bidderUsername && !bidderUsername.isBlank()) {
                leaderUsernameCache.put(productId, bidderUsername);
                leaderCacheLoaded.add(productId);
            }

            break;
        }

        productTable.refresh();
        updateDashboardMessages();
    }

    @FXML
    private void handleViewDetail() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            AlertUtil.showError("Vui lòng chọn sản phẩm trước");
            return;
        }

        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/ProductDetail.fxml");
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(selectedProduct);
            controller.setUsername(username);


            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 1200, 700));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Không thể mở trang chi tiết sản phẩm");
        }
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
        LocalDateTime now = LocalDateTime.now();

        if (product.getStartTime() != null && product.getStartTime().isAfter(now)) {
            return "COMING SOON";
        }

        String leaderUsername = getCachedLeaderUsername(product.getId());

        if (product.getEndTime() != null &&
                !product.getEndTime().isAfter(now)) {
            return leaderUsername == null || leaderUsername.isBlank()
                    ? "FINISHED"
                    : "FINISHED - Winner: " + leaderUsername;
        }

        return leaderUsername == null || leaderUsername.isBlank()
                ? "OPENING"
                : "OPENING - Current Leader: " + leaderUsername;
    }

    private String getCachedLeaderUsername(int productId) {
        if (!leaderCacheLoaded.contains(productId)) {
            leaderUsernameCache.put(productId, bidService.getWinnerUsernameByProductId(productId));
            leaderCacheLoaded.add(productId);
        }

        return leaderUsernameCache.get(productId);
    }

    private void startCountdownTimer() {
        countdownTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    productTable.refresh();
                    updateDashboardMessages();
                })
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateDashboardMessages() {
        if (productTable == null) {
            return;
        }

        long availableProductCount = productTable.getItems() == null
                ? 0
                : productTable.getItems().stream().filter(this::isAuctionAvailable).count();

        updateOpeningNotice(availableProductCount);
        updateEmptyStateMessage(availableProductCount);
    }

    private void updateOpeningNotice(long availableProductCount) {
        if (openingNoticeLabel == null) {
            return;
        }

        openingNoticeLabel.setText("Quy định: Nếu có bidder đặt giá trong vòng 15 giây cuối, phiên đấu giá sẽ tự động gia hạn thêm 15 giây.");
        openingNoticeLabel.setVisible(true);
        openingNoticeLabel.setManaged(true);
    }

    private void updateEmptyStateMessage(long availableProductCount) {
        if (emptyStateLabel == null) {
            return;
        }

        boolean hasAvailableProducts = availableProductCount > 0;
        if (hasAvailableProducts) {
            emptyStateLabel.setText("Có " + availableProductCount + " sản phẩm đang đấu giá,hãy vào đấu giá ngay!");
        } else {
            emptyStateLabel.setText("Hiện tại chưa có sản phẩm đấu giá khả dụng.");
        }

        emptyStateLabel.setVisible(true);
        emptyStateLabel.setManaged(true);
    }

    private boolean isAuctionAvailable(Product product) {
        if (product == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (product.getStartTime() != null && product.getStartTime().isAfter(now)) {
            return false;
        }

        if (product.getEndTime() != null && !product.getEndTime().isAfter(now)) {
            return false;
        }

        String status = product.getStatus();
        return status != null
                && !status.equalsIgnoreCase("DELETED")
                && !status.equalsIgnoreCase("CLOSED");
    }

}



