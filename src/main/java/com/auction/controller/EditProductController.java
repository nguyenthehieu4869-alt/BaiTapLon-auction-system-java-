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
        if (product == null) {
            AlertUtil.showError("Khong tim thay san pham can cap nhat");
            return;
        }

        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = startPriceField.getText().trim();
        String status = statusComboBox.getValue();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || status == null) {
            AlertUtil.showError("Vui long ghi day du thong tin bat buoc");
            return;
        }

        double startPrice;

        try {
            startPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui long nhap so");
            return;
        }

        if (startPrice <= 0) {
            AlertUtil.showError("Gia phai lon hon 0");
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
            AlertUtil.showInfo("Cap nhat thanh cong");
            closeWindow();
        } else {
            AlertUtil.showError("Cap nhat that bai");
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
            imageStatusLabel.setText("Chua tai anh. Ban co the bo qua muc nay.");
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            productImagePreview.setImage(null);
            imageStatusLabel.setText("Anh da luu khong con ton tai. Ban co the chon anh khac.");
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
