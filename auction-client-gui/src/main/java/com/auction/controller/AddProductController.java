package com.auction.controller;

import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;

public class AddProductController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startPriceField;

    @FXML
    private TextField durationMinutesField;

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
        fileChooser.setTitle("Chon anh san pham");
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
        String durationText = durationMinutesField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || durationText.isEmpty()) {
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

        int durationMinutes;

        try {
            durationMinutes = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui lòng nhập thời lượng đấu giá hợp lệ !");
            return;
        }

        if (durationMinutes <= 0) {
            AlertUtil.showError("Vui lòng nhập thời lượng đấu giá hợp lệ !");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

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
            AlertUtil.showError("Thêm sản phẩm thất bại !");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
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

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Khong tim thay anh da chon. Hay thu lai.");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = new Image(imageFile.toURI().toString(), 180, 120, true, true);
            if (image.isError()) {
                productImagePreview.setImage(null);
                imageStatusLabel.setText("Khong doc duoc anh. Hay chon file khac.");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImagePreview.setImage(image);
            imageStatusLabel.setText("Da chon anh: " + imageFile.getName());
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Khong mo duoc anh. Hay chon file khac.");
            imagePlaceholderLabel.setVisible(true);
        }
    }

    private void configureInitialDirectory(FileChooser fileChooser) {
        if (selectedImagePath == null || selectedImagePath.isBlank()) {
            return;
        }

        File currentFile = new File(selectedImagePath);
        File directory = currentFile.isDirectory() ? currentFile : currentFile.getParentFile();

        if (directory != null && directory.exists()) {
            fileChooser.setInitialDirectory(directory);
        }
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
