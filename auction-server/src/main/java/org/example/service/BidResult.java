package org.example.service;

import java.time.LocalDateTime;

public class BidResult {
    private final boolean success;
    private final String message;
    private final double currentPrice;
    private final LocalDateTime endTime;
    private final String productName;

    public BidResult(boolean success, String message, double currentPrice, LocalDateTime endTime, String productName) {
        this.success = success;
        this.message = message;
        this.currentPrice = currentPrice;
        this.endTime = endTime;
        this.productName = productName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getProductName() {
        return productName;
    }

    public static BidResult failure(String message) {
        return new BidResult(false, message, 0, null, null);
    }

    public static BidResult success(String message, double currentPrice, LocalDateTime endTime, String productName) {
        return new BidResult(true, message, currentPrice, endTime, productName);
    }
}
