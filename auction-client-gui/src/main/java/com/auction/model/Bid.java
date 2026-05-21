package com.auction.model;

import java.sql.Timestamp;

public class Bid {
    private int id;
    private int productId;
    private String bidderUsername;
    private double bidPrice;
    private Timestamp bidTime;

    public Bid(int id, int productId, String bidderUsername, double bidPrice, Timestamp bidTime) {
        this.id = id;
        this.productId = productId;
        this.bidderUsername = bidderUsername;
        this.bidPrice = bidPrice;
        this.bidTime = bidTime;
    }

    public int getId() {
        return id;
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

    public Timestamp getBidTime() {
        return bidTime;
    }
}
