package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.ProductDAO;
import com.auction.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
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
    private ComboBox<String> statusComboBox;

    @FXML
    private ImageView productImagePreview;

    @FXML
    private Label imageStatusLabel;

    @FXML
    private Label imagePlaceholderLabel;

    private Product product;

    private String selectedImagePath;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    private void initialize() {
        statusComboBox.getItems().addAll("OPEN", "CLOSED");
        updateImagePreview(null);
    }

    public void setProduct(Product product) {
        this.product = product;

        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        startPriceField.setText(String.valueOf(product.getStartPrice()));
        statusComboBox.setValue(product.getStatus());

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
        String status = statusComboBox.getValue();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || status == null) {
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

        boolean success = productDAO.updateProduct(
                product.getId(),
                name,
                description,
                startPrice,
                status,
                selectedImagePath
        );

        if (success) {
            AlertUtil.showInfo("Cập nhật thành công !");
            closeWindow();
        } else {
            AlertUtil.showError("Cập nhật thất bại !");
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

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Ảnh đã lưu không tồn tại !");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        try {
            Image image = new Image(imageFile.toURI().toString(), 180, 120, true, true);
            if (image.isError()) {
                productImagePreview.setImage(null);
                imageStatusLabel.setText(" Không đọc được ảnh !");
                imagePlaceholderLabel.setVisible(true);
                return;
            }

            productImagePreview.setImage(image);
            imageStatusLabel.setText("Đã chọn ảnh : " + imageFile.getName());
            imagePlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Không mở được ảnh !");
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
