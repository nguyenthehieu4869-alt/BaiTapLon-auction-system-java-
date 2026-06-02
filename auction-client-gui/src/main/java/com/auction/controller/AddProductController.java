package com.auction.controller;

import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import com.auction.util.ProductImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.auction.common.AuctionTime;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddProductController {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startPriceField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField endTimeField;

    @FXML
    private ImageView productImagePreview;

    @FXML
    private Label imageStatusLabel;

    @FXML
    private Label imagePlaceholderLabel;

    private final RemoteProductService productService = new RemoteProductService();

    private String sellerUsername;

    private String selectedImagePath;

    @FXML
    private void initialize() {
        updateImagePreview(null);
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"
                )
        );

        configureInitialDirectory(fileChooser);

        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile == null) {
            return;
        }

        selectedImagePath = selectedFile.getAbsolutePath();
        updateImagePreview(selectedImagePath);
    }

    @FXML
    private void handleRemoveImage() {
        selectedImagePath = null;
        updateImagePreview(null);
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = startPriceField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập đầy đủ thông tin !");
            return;
        }

        double startPrice;

        try {
            startPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui lòng nhập giá khởi điểm hợp lệ !");
            return;
        }

        if (startPrice <= 0) {
            AlertUtil.showError("Giá khởi điểm phải lớn hơn 0 !");
            return;
        }

        LocalDateTime startTime = parseAuctionDateTime(startDatePicker, startTimeField, "bắt đầu");
        if (startTime == null) {
            return;
        }

        LocalDateTime endTime = parseAuctionDateTime(endDatePicker, endTimeField, "kết thúc");
        if (endTime == null) {
            return;
        }

        LocalDateTime now = AuctionTime.now();

        if (!startTime.isAfter(now)) {
            AlertUtil.showError("Thoi diem bat dau phai sau thoi diem hien tai !");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            AlertUtil.showError("Thời điểm kết thúc phải sau thời điểm bắt đầu !");
            return;
        }

        if (!endTime.isAfter(now)) {
            AlertUtil.showError("Thời điểm kết thúc phải sau thời điểm hiện tại !");
            return;
        }

        if (sellerUsername == null || sellerUsername.isBlank()) {
            AlertUtil.showError("Không xác định được tài khoản seller !");
            return;
        }

        boolean success = productService.addProduct(
                name,
                description,
                startPrice,
                startTime,
                endTime,
                sellerUsername,
                selectedImagePath
        );

        if (success) {
            AlertUtil.showInfo("Thêm  sản phẩm thành công !");
            closeWindow();
        } else {
            AlertUtil.showError(getSaveErrorMessage("Thêm sản phẩm thất bại !"));
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private LocalDateTime parseAuctionDateTime(DatePicker datePicker, TextField timeField, String fieldName) {
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText().trim();

        if (date == null || timeText.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập thời điểm " + fieldName + " !");
            return null;
        }

        try {
            LocalTime time = LocalTime.parse(timeText, TIME_FORMATTER);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException e) {
            AlertUtil.showError("Vui lòng nhập giờ " + fieldName + " theo định dạng HH:mm !");
            return null;
        }
    }

    private void updateImagePreview(String imagePath) {
        if (productImagePreview == null || imageStatusLabel == null || imagePlaceholderLabel == null) {
            return;
        }

        if (imagePath == null || imagePath.isBlank()) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Tải ảnh lên ở đây");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = ProductImageUtil.loadImage(imagePath, 180, 120);
            if (image.isError()) {
                productImagePreview.setImage(null);
                imageStatusLabel.setText("Không đọc được ảnh. Hãy chọn file khác.");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImagePreview.setImage(image);
            imageStatusLabel.setText("Đã chọn ảnh: " + ProductImageUtil.getDisplayName(imagePath));
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Không mở được ảnh. Hãy chọn file khác.");
            imagePlaceholderLabel.setVisible(true);
        }
    }

    private void configureInitialDirectory(FileChooser fileChooser) {
        if (selectedImagePath == null
                || selectedImagePath.isBlank()
                || ProductImageUtil.isEmbeddedImage(selectedImagePath)) {
            return;
        }

        File currentFile = new File(selectedImagePath);
        File directory = currentFile.isDirectory() ? currentFile : currentFile.getParentFile();

        if (directory != null && directory.exists()) {
            fileChooser.setInitialDirectory(directory);
        }
    }

    private String getSaveErrorMessage(String fallbackMessage) {
        String errorMessage = productService.getLastErrorMessage();
        return errorMessage == null || errorMessage.isBlank() ? fallbackMessage : errorMessage;
    }

    private void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getStage() {
        return (Stage) nameField.getScene().getWindow();
    }
}
