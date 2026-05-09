package com.auction.controller;

import com.auction.model.Product;
import com.auction.service.ProductDAO;
import com.auction.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;




public class EditProductController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startPriceField;

    @FXML
    private ComboBox<String> statusComboBox;

    private Product product;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    private void initialize(){
        statusComboBox.getItems().addAll("OPEN","CLOSED");
    }

    public void setProduct(Product product) {
        this.product = product;

        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        startPriceField.setText(String.valueOf(product.getStartPrice()));
        statusComboBox.setValue(product.getStatus());
    }

    @FXML
    private void handleSave(){
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = startPriceField.getText().trim();
        String status = statusComboBox.getValue();

        if(name.isEmpty() || description.isEmpty() || priceText.isEmpty() || status == null){
            AlertUtil.showError("Vui lòng ghi đầy đủ thông tin !");
            return;
        }

        double startPrice;

        try {
            startPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Vui lòng nhập số !");
            return;
        }

        if (startPrice <= 0){
            AlertUtil.showError("Giá phải lớn hơn 0 !");
            return;
        }

        boolean success = productDAO.updateProduct(
                product.getId(),name,description,
                startPrice,status);

        if (success){
            AlertUtil.showInfo("Cập nhật thành công !");
            closeWindow();
        }else {
            AlertUtil.showError("Cập nhật thất bại !");
        }
    }

    @FXML
    private void handleCancel(){
        closeWindow();
    }

    private void closeWindow(){
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

}

