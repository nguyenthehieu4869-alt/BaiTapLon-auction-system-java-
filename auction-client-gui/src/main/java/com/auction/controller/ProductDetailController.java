package com.auction.controller;

import com.auction.model.Bid;
import com.auction.model.Product;
import com.auction.network.AuctionNetworkClient;
import com.auction.network.BidUpdateListener;
import com.auction.service.BidResult;
import com.auction.service.remote.RemoteBidService;
import com.auction.util.AlertUtil;
import com.auction.util.BidNotificationUtil;
import com.auction.util.FxmlUtil;
import com.auction.util.PriceFormatter;
import com.auction.util.ProductImageUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.auction.common.AuctionTime;
import com.auction.common.ProductStatus;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailController {
    private static final DateTimeFormatter PRODUCT_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter BID_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter CHART_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat CHART_PRICE_FORMATTER = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final double SINGLE_POINT_PADDING_SECONDS = 60;
    private static final int CHART_GRID_INTERVAL_COUNT = 5;
    private static final double[] CHART_TIME_TICK_UNITS = {
            1, 2, 5, 10, 15, 30,
            60, 120, 300, 600, 900, 1800,
            3600, 7200, 10800, 21600, 43200, 86400
    };

    @FXML
    private Label productNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label startPriceLabel;

    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label endTimeLabel;

    @FXML
    private ImageView productImageView;

    @FXML
    private Label imagePlaceholderLabel;

    @FXML
    private TextField bidAmountField;

    @FXML
    private TableView<Bid> bidHistoryTable;

    @FXML
    private LineChart<Number, Number> priceChart;

    @FXML
    private NumberAxis priceChartXAxis;

    @FXML
    private NumberAxis priceChartYAxis;

    @FXML
    private TableColumn<Bid, String> bidderUsernameColumn;

    @FXML
    private TableColumn<Bid, Double> bidPriceColumn;

    @FXML
    private TableColumn<Bid, java.sql.Timestamp> bidTimeColumn;

    @FXML
    private Button placeBidButton;

    private Product product;

    private final RemoteBidService bidService = new RemoteBidService();
    private final XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();
    private final BidUpdateListener bidUpdateListener = this::handleBidUpdate;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void initialize() {
        setupBidHistoryTable();
        setupPriceChart();
        AuctionNetworkClient.getInstance().addBidUpdateListener(bidUpdateListener);
    }

    @FXML
    private void handlePlaceBid() {
        try {
            double newPrice = Double.parseDouble(bidAmountField.getText().trim());

            if (isAuctionEnded()) {
                AlertUtil.showError("Phiên đấu giá đã kết thúc, không thể đặt giá.");
                return;
            }

            if (isAuctionNotStarted()) {
                AlertUtil.showError("Phiên đấu giá chưa bắt đầu, không thể đặt giá.");
                return;
            }

            BidResult result = bidService.placeBid(product, username, newPrice);

            if (!result.isSuccess()) {
                AlertUtil.showError(result.getMessage());
                return;
            }

            product.setCurrentPrice(newPrice);
            updateProductLabels();
            bidAmountField.clear();
            loadBidHistory();

            AlertUtil.showInfo(result.getMessage());

        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui lòng nhập giá hợp lệ");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/home.fxml");
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUser(username);

            dispose();

            Stage stage = (Stage) productNameLabel.getScene().getWindow();
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

        updateProductLabels();
        updateProductImage(product.getImagePath());
        loadBidHistory();
        updateBidControls();
    }

    private void setupBidHistoryTable() {
        if (bidHistoryTable == null) {
            return;
        }

        bidHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        bidHistoryTable.setFixedCellSize(34);
        bidHistoryTable.setPlaceholder(new Label("Chưa có lượt đặt giá"));

        bidderUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("bidderUsername"));
        bidPriceColumn.setCellValueFactory(new PropertyValueFactory<>("bidPrice"));
        bidTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bidTime"));

        bidPriceColumn.setCellFactory(column -> new TableCell<Bid, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : PriceFormatter.formatVND(price));
            }
        });

        bidTimeColumn.setCellFactory(column -> new TableCell<Bid, java.sql.Timestamp>() {
            @Override
            protected void updateItem(java.sql.Timestamp time, boolean empty) {
                super.updateItem(time, empty);
                setText(empty || time == null ? null : BID_TIME_FORMATTER.format(time.toLocalDateTime()));
            }
        });
    }

    private void setupPriceChart() {
        if (priceChart == null || priceChartXAxis == null || priceChartYAxis == null) {
            return;
        }

        priceSeries.setName("Giá đấu");
        priceChart.getData().clear();
        priceChart.getData().add(priceSeries);
        priceChart.setAnimated(false);
        priceChart.setLegendVisible(false);
        priceChart.setCreateSymbols(true);
        priceChart.setMinHeight(210);
        priceChart.setPrefHeight(230);
        priceChart.setMaxHeight(260);
        priceChart.setMinWidth(0);
        priceChart.setMaxWidth(Double.MAX_VALUE);
        priceChart.setHorizontalGridLinesVisible(true);
        priceChart.setVerticalGridLinesVisible(true);
        priceChart.setVerticalZeroLineVisible(false);
        priceChart.setHorizontalZeroLineVisible(false);

        priceChartXAxis.setForceZeroInRange(false);
        priceChartXAxis.setTickLabelGap(6);
        priceChartXAxis.setTickLabelRotation(-35);
        priceChartXAxis.setTickLabelsVisible(true);
        priceChartXAxis.setTickMarkVisible(true);
        priceChartXAxis.setMinorTickVisible(false);
        priceChartXAxis.setAutoRanging(false);
        priceChartXAxis.setLabel("Thời gian");
        priceChartXAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                if (value == null) {
                    return "";
                }

                return formatChartTime(value.doubleValue());
            }

            @Override
            public Number fromString(String value) {
                return 0;
            }
        });

        priceChartYAxis.setForceZeroInRange(false);
        priceChartYAxis.setTickLabelGap(6);
        priceChartYAxis.setTickLabelsVisible(true);
        priceChartYAxis.setTickMarkVisible(true);
        priceChartYAxis.setMinorTickVisible(false);
        priceChartYAxis.setAutoRanging(false);
        priceChartYAxis.setLabel("Giá thầu");
        priceChartYAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return value == null ? "" : CHART_PRICE_FORMATTER.format(value.doubleValue());
            }

            @Override
            public Number fromString(String value) {
                return 0;
            }
        });
    }

    private void refreshPriceChart(List<Bid> sourceBids) {
        if (priceSeries == null || product == null) {
            return;
        }

        List<Bid> chartBids = new ArrayList<>(sourceBids == null ? List.of() : sourceBids);
        chartBids.removeIf(bid -> bid.getBidTime() == null);
        chartBids.sort(this::compareBidTimeAscending);

        priceSeries.getData().clear();

        if (chartBids.isEmpty()) {
            double now = Instant.now().getEpochSecond();
            updateTimeAxis(now - SINGLE_POINT_PADDING_SECONDS, now + SINGLE_POINT_PADDING_SECONDS);
            updatePriceAxis(product.getCurrentPrice(), product.getCurrentPrice());
            return;
        }

        double minTime = Double.MAX_VALUE;
        double maxTime = Double.MIN_VALUE;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        for (Bid bid : chartBids) {
            double bidTime = bid.getBidTime().getTime() / 1000.0;
            double price = bid.getBidPrice();

            priceSeries.getData().add(new XYChart.Data<>(bidTime, price));
            minTime = Math.min(minTime, bidTime);
            maxTime = Math.max(maxTime, bidTime);
            minPrice = Math.min(minPrice, price);
            maxPrice = Math.max(maxPrice, price);
        }

        updateTimeAxis(minTime, maxTime);
        updatePriceAxis(minPrice, maxPrice);
    }

    private void updateTimeAxis(double minTime, double maxTime) {
        double range = Math.max(1, maxTime - minTime);
        double padding = range <= 1 ? SINGLE_POINT_PADDING_SECONDS : Math.max(30, range * 0.08);
        double tickUnit = chooseTimeTickUnit(((maxTime + padding) - (minTime - padding)) / CHART_GRID_INTERVAL_COUNT);
        double lowerBound = Math.floor((minTime - padding) / tickUnit) * tickUnit;
        double upperBound = Math.ceil((maxTime + padding) / tickUnit) * tickUnit;

        priceChartXAxis.setLowerBound(lowerBound);
        priceChartXAxis.setUpperBound(Math.max(lowerBound + tickUnit, upperBound));
        priceChartXAxis.setTickUnit(tickUnit);
    }

    private void updatePriceAxis(double minPrice, double maxPrice) {
        double range = Math.max(1, maxPrice - minPrice);
        double padding = Math.max(1, range * 0.1);

        if (Double.compare(minPrice, maxPrice) == 0) {
            padding = Math.max(1, maxPrice * 0.05);
        }

        double lowerBound = Math.max(0, minPrice - padding);
        double upperBound = maxPrice + padding;
        double tickUnit = chooseNiceTickUnit((upperBound - lowerBound) / CHART_GRID_INTERVAL_COUNT);

        lowerBound = Math.max(0, Math.floor(lowerBound / tickUnit) * tickUnit);
        upperBound = Math.ceil(upperBound / tickUnit) * tickUnit;

        if (Double.compare(lowerBound, upperBound) >= 0) {
            upperBound = lowerBound + tickUnit;
        }

        priceChartYAxis.setLowerBound(lowerBound);
        priceChartYAxis.setUpperBound(upperBound);
        priceChartYAxis.setTickUnit(tickUnit);
    }

    private double chooseTimeTickUnit(double targetTickUnit) {
        for (double tickUnit : CHART_TIME_TICK_UNITS) {
            if (tickUnit >= targetTickUnit) {
                return tickUnit;
            }
        }

        return Math.ceil(targetTickUnit / 86400) * 86400;
    }

    private double chooseNiceTickUnit(double targetTickUnit) {
        if (targetTickUnit <= 0) {
            return 1;
        }

        double magnitude = Math.pow(10, Math.floor(Math.log10(targetTickUnit)));
        double normalizedTickUnit = targetTickUnit / magnitude;

        if (normalizedTickUnit <= 1) {
            return magnitude;
        }

        if (normalizedTickUnit <= 2) {
            return 2 * magnitude;
        }

        if (normalizedTickUnit <= 5) {
            return 5 * magnitude;
        }

        return 10 * magnitude;
    }

    private void loadBidHistory() {
        if (product == null) {
            return;
        }

        List<Bid> bids = new ArrayList<>(bidService.getBidsByProductId(product.getId()));
        List<Bid> newestFirst = new ArrayList<>(bids);
        newestFirst.sort(this::compareBidTimeDescending);

        bidHistoryTable.setItems(FXCollections.observableArrayList(newestFirst));
        refreshPriceChart(bids);
    }

    private int compareBidTimeAscending(Bid first, Bid second) {
        if (first.getBidTime() == null && second.getBidTime() == null) {
            return Integer.compare(first.getId(), second.getId());
        }

        if (first.getBidTime() == null) {
            return 1;
        }

        if (second.getBidTime() == null) {
            return -1;
        }

        int timeCompare = first.getBidTime().compareTo(second.getBidTime());
        return timeCompare != 0 ? timeCompare : Integer.compare(first.getId(), second.getId());
    }

    private int compareBidTimeDescending(Bid first, Bid second) {
        if (first.getBidTime() == null && second.getBidTime() == null) {
            return Integer.compare(second.getId(), first.getId());
        }

        if (first.getBidTime() == null) {
            return 1;
        }

        if (second.getBidTime() == null) {
            return -1;
        }

        int timeCompare = second.getBidTime().compareTo(first.getBidTime());
        return timeCompare != 0 ? timeCompare : Integer.compare(second.getId(), first.getId());
    }

    private void updateBidControls() {
        boolean unavailable = isAuctionEnded() || isAuctionNotStarted();
        bidAmountField.setDisable(unavailable);
        placeBidButton.setDisable(unavailable);
    }

    private void handleBidUpdate(com.auction.common.network.protocol.Message message) {
        if (message == null || message.getData() == null) {
            return;
        }

        BidNotificationUtil.showBidNotification(
                productNameLabel.getScene() == null ? null : productNameLabel.getScene().getWindow(),
                username,
                message
        );

        if (product == null) {
            return;
        }

        Object dataObj = message.getData();
        if (!(dataObj instanceof Map<?, ?> data)) {
            return;
        }

        Object productIdObj = data.get("productId");
        Object currentPriceObj = data.get("currentPrice");
        Object endTimeObj = data.get("endTime");

        if (!(productIdObj instanceof Number productIdNumber) || productIdNumber.intValue() != product.getId()) {
            return;
        }

        if (currentPriceObj instanceof Number currentPriceNumber) {
            product.setCurrentPrice(currentPriceNumber.doubleValue());
        }

        if (endTimeObj instanceof String endTimeText && !endTimeText.isBlank()) {
            try {
                product.setEndTime(LocalDateTime.parse(endTimeText));
            } catch (Exception ignored) {
                // Ignore malformed push data; the next full product refresh will correct the timestamp.
            }
        }

        updateProductLabels();
        loadBidHistory();
        updateBidControls();
    }

    private void updateProductLabels() {
        if (product == null) {
            return;
        }

        productNameLabel.setText("Tên: " + textOrFallback(product.getName()));
        descriptionLabel.setText("Mô tả: " + textOrFallback(product.getDescription()));
        startPriceLabel.setText("Giá khởi điểm: " + PriceFormatter.formatVND(product.getStartPrice()));
        currentPriceLabel.setText("Giá hiện tại: " + PriceFormatter.formatVND(product.getCurrentPrice()));
        statusLabel.setText("Trạng thái: " + getDisplayStatus());
        startTimeLabel.setText("Bắt đầu: " + formatTime(product.getStartTime()));
        endTimeLabel.setText("Kết thúc: " + formatTime(product.getEndTime()));
    }

    private String textOrFallback(String value) {
        return value == null || value.isBlank() ? "Chưa có thông tin" : value;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "Chưa xác định" : PRODUCT_TIME_FORMATTER.format(time);
    }

    private String formatChartTime(double epochSeconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(Math.round(epochSeconds)),
                ZoneId.systemDefault()
        ).format(CHART_TIME_FORMATTER);
    }

    private boolean isAuctionEnded() {
        if (product == null) {
            return true;
        }

        return ProductStatus.isFinished(product.getStatus(), product.getEndTime());
    }

    private boolean isAuctionNotStarted() {
        return product == null
                || (product.getStartTime() != null
                && product.getStartTime().isAfter(AuctionTime.now()));
    }

    private String getDisplayStatus() {
        return ProductStatus.current(product.getStartTime(), product.getEndTime(), product.getStatus());
    }

    private void updateProductImage(String imagePath) {
        if (productImageView == null || imagePlaceholderLabel == null) {
            return;
        }

        if (imagePath == null || imagePath.isBlank()) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setText("Chưa có ảnh sản phẩm");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = ProductImageUtil.loadImage(imagePath, 198, 138);

            if (image.isError()) {
                productImageView.setImage(null);
                imagePlaceholderLabel.setText("Không tải được ảnh");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImageView.setImage(image);
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setText("Không tải được ảnh");
            imagePlaceholderLabel.setVisible(true);
        }
    }

    @FXML
    private void dispose() {
        AuctionNetworkClient.getInstance().removeBidUpdateListener(bidUpdateListener);
    }
}
