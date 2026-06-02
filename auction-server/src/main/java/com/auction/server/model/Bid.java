package com.auction.server.model;

public class Bid {

    private int userId;
    private int productId;
    private double amount;

    public Bid(int userId, int productId, double amount) {
        this.userId = userId;
        this.productId = productId;
        this.amount = amount;
    }

    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public double getAmount() { return amount; }
}

