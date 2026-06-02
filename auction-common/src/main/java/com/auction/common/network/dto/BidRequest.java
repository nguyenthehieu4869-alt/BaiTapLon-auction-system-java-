package com.auction.common.network.dto;

public class BidRequest {
    private int productId;
    private String bidderUsername;
    private double bidPrice;

    public BidRequest(int productId, String bidderUsername, double bidPrice) {
        this.productId = productId;
        this.bidderUsername = bidderUsername;
        this.bidPrice = bidPrice;
    }

    public int getProductId() {
        return productId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public double getBidPrice() {
        return bidPrice;
    }
}