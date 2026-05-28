package com.auction.service.remote;

import com.auction.model.Product;
import com.auction.network.AuctionNetworkClient;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.common.ProductStatus;
import org.example.network.dto.ProductDTO;
import org.example.network.dto.ProductSaveRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteProductService {
    private String lastErrorMessage;

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public ObservableList<Product> getAllProducts() {
        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.GET_PRODUCTS, null, true)
        );

        return parseProductList(response);
    }

    public ObservableList<Product> getProductsBySeller(String sellerUsername) {
        Map<String, Object> data = new HashMap<>();
        data.put("sellerUsername", sellerUsername);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.GET_PRODUCTS_BY_SELLER, data, true)
        );

        return parseProductList(response);
    }

    public boolean addProduct(String name, String description, double startPrice,
                              LocalDateTime startTime, LocalDateTime endTime,
                              String sellerUsername, String imagePath) {
        ProductSaveRequest request = new ProductSaveRequest(
                0,
                name,
                description,
                imagePath,
                startPrice,
                ProductStatus.COMING_SOON,
                startTime.toString(),
                endTime.toString(),
                sellerUsername
        );

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.ADD_PRODUCT, request, true)
        );

        return response.isSuccess();
    }

    public boolean updateProduct(int id, String name, String description,
                                 double startPrice, String status, String imagePath) {
        ProductSaveRequest request = new ProductSaveRequest(
                id,
                name,
                description,
                imagePath,
                startPrice,
                status,
                null,
                null,
                null
        );

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.EDIT_PRODUCT, request, true)
        );

        return response.isSuccess();
    }

    public boolean deleteProduct(int productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.DELETE_PRODUCT, data, true)
        );

        return response.isSuccess();
    }

    public boolean closeAuction(int productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.CLOSE_AUCTION, data, true)
        );

        return response.isSuccess();
    }

    private ObservableList<Product> parseProductList(Message response) {
        lastErrorMessage = null;

        if (response == null) {
            lastErrorMessage = "Server không phản hồi.";
            return FXCollections.observableArrayList();
        }

        if (!response.isSuccess()) {
            lastErrorMessage = response.getMessage() == null || response.getMessage().isBlank()
                    ? "Server trả lỗi khi tải sản phẩm."
                    : response.getMessage();
            return FXCollections.observableArrayList();
        }

        if (response.getData() == null) {
            lastErrorMessage = "Server không gửi dữ liệu sản phẩm.";
            return FXCollections.observableArrayList();
        }

        try {
            Type type = new TypeToken<List<ProductDTO>>() {}.getType();
            List<ProductDTO> dtos = Protocol.gson().fromJson(
                    Protocol.gson().toJson(response.getData()),
                    type
            );

            ObservableList<Product> products = FXCollections.observableArrayList();

            if (dtos == null) {
                return products;
            }

            for (ProductDTO dto : dtos) {
                products.add(toProduct(dto));
            }

            return products;
        } catch (Exception e) {
            lastErrorMessage = "Không đọc được dữ liệu sản phẩm từ server: " + e.getMessage();
            return FXCollections.observableArrayList();
        }
    }

    private Product toProduct(ProductDTO dto) {
        Product product = new Product(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getStartPrice(),
                dto.getCurrentPrice(),
                dto.getStatus()
        );

        product.setImagePath(dto.getImagePath());

        if (dto.getStartTime() != null) {
            product.setStartTime(LocalDateTime.parse(dto.getStartTime()));
        }

        if (dto.getEndTime() != null) {
            product.setEndTime(LocalDateTime.parse(dto.getEndTime()));
        }

        return product;
    }
}
