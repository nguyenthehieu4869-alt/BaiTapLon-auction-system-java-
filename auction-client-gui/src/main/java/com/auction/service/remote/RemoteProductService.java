package com.auction.service.remote;

import com.auction.model.Product;
import com.auction.network.AuctionNetworkClient;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        int durationMinutes = Math.max(1, (int) java.time.Duration.between(startTime, endTime).toMinutes());

        ProductSaveRequest request = new ProductSaveRequest(
                0,
                name,
                description,
                imagePath,
                startPrice,
                "OPEN",
                durationMinutes,
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
                0,
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
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return FXCollections.observableArrayList();
        }

        Type type = new TypeToken<List<ProductDTO>>() {}.getType();
        List<ProductDTO> dtos = Protocol.gson().fromJson(
                Protocol.gson().toJson(response.getData()),
                type
        );

        ObservableList<Product> products = FXCollections.observableArrayList();

        for (ProductDTO dto : dtos) {
            products.add(toProduct(dto));
        }

        return products;
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