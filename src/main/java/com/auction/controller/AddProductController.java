package com.auction.controller;

import com.auction.service.ProductDAO;
import javafx.fxml.FXML;
import com.auction.util.AlertUtil;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startPriceField;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = startPriceField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        double startPrice;

        try {
            startPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Giá khởi điểm phải là số");
            return;
        }

        if (startPrice <= 0) {
            AlertUtil.showError("Giá khởi điểm phải lớn hơn 0");
            return;
        }

        boolean success = productDAO.addProduct(name, description, startPrice);

        if (success) {
            AlertUtil.showInfo("Thêm sản phẩm thành công");
            closeWindow();
        } else {
            AlertUtil.showError("Thêm sản phẩm thất bại");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

}