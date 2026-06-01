package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.remote.RemoteProductService;
import com.auction.util.AlertUtil;
import com.auction.util.ProductImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class EditProductController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startPriceField;

    @FXML
    private ImageView productImagePreview;

    @FXML
    private Label imageStatusLabel;

    @FXML
    private Label imagePlaceholderLabel;

    private Product product;

    private String selectedImagePath;

    private final RemoteProductService productService = new RemoteProductService();

    @FXML
    private void initialize() {
        updateImagePreview(null);
    }

    public void setProduct(Product product) {
        this.product = product;

        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        startPriceField.setText(String.valueOf(product.getStartPrice()));

        selectedImagePath = product.getImagePath();
        updateImagePreview(selectedImagePath);
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
        if (product == null) {
            AlertUtil.showError("Không tìm thấy sản phẩm !");
            return;
        }

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
            AlertUtil.showError("Vui lòng nhập giá khởi điểm hợp lệ !");
            return;
        }

        boolean success = productService.updateProduct(
                product.getId(),
                name,
                description,
                startPrice,
                product.getStatus(),
                selectedImagePath
        );

        if (success) {
            AlertUtil.showInfo("Cập nhật thành công !");
            closeWindow();
        } else {
            AlertUtil.showError(getSaveErrorMessage("Cập nhật thất bại !"));
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
            imageStatusLabel.setText("Bạn có thể bỏ qua việc tải ảnh.");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = ProductImageUtil.loadImage(imagePath, 180, 120);
            if (image.isError()) {
                productImagePreview.setImage(null);
                imageStatusLabel.setText(" Không đọc được ảnh !");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImagePreview.setImage(image);
            imageStatusLabel.setText("Đã chọn ảnh : " + ProductImageUtil.getDisplayName(imagePath));
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Không mở được ảnh !");
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
