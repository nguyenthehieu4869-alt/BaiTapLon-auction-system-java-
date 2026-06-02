package com.auction.service.remote;

import com.auction.model.Bid;
import com.auction.model.Product;
import com.auction.network.AuctionNetworkClient;
import com.auction.service.BidResult;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.auction.common.network.dto.BidDTO;
import com.auction.common.network.dto.BidRequest;
import com.auction.common.network.protocol.Message;
import com.auction.common.network.protocol.MessageType;
import com.auction.common.network.protocol.Protocol;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteBidService {

    public BidResult placeBid(Product product, String username, double newPrice) {
        BidRequest request = new BidRequest(product.getId(), username, newPrice);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.PLACE_BID, request, true)
        );

        if (response.isSuccess()) {
            product.setCurrentPrice(newPrice);
            return BidResult.success(response.getMessage() == null ? "Đặt giá thành công" : response.getMessage());
        }

        return BidResult.failure(response.getMessage() == null ? "Đặt giá thất bại" : response.getMessage());
    }

    public ObservableList<Bid> getBidsByProductId(int productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.GET_BID_HISTORY, data, true)
        );

        if (!response.isSuccess() || response.getData() == null) {
            return FXCollections.observableArrayList();
        }

        Type type = new TypeToken<List<BidDTO>>() {}.getType();
        List<BidDTO> dtos = Protocol.gson().fromJson(
                Protocol.gson().toJson(response.getData()),
                type
        );

        ObservableList<Bid> bids = FXCollections.observableArrayList();

        for (BidDTO dto : dtos) {
            bids.add(new Bid(
                    dto.getId(),
                    dto.getProductId(),
                    dto.getBidderUsername(),
                    dto.getBidPrice(),
                    parseBidTimestamp(dto.getBidTime())
            ));
        }

        return bids;
    }

    public String getWinnerUsernameByProductId(int productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);

        Message response = AuctionNetworkClient.getInstance().sendAndWait(
                new Message(MessageType.GET_WINNER, data, true)
        );

        if (!response.isSuccess() || response.getData() == null) {
            return null;
        }

        return response.getData().toString();
    }

    private Timestamp parseBidTimestamp(String bidTime) {
        if (bidTime == null || bidTime.isBlank()) {
            return null;
        }

        try {
            return Timestamp.valueOf(LocalDateTime.parse(bidTime));
        } catch (DateTimeParseException ignored) {
            return Timestamp.valueOf(bidTime);
        }
    }
}
