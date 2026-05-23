package org.example.service;

import java.time.LocalDateTime;

public class BidResult {
    private final boolean success;
    private final String message;
    private final double currentPrice;
    private final LocalDateTime endTime;

    public BidResult(boolean success, String message, double currentPrice, LocalDateTime endTime) {
        this.success = success;
        this.message = message;
        this.currentPrice = currentPrice;
        this.endTime = endTime;
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

    public static BidResult failure(String message) {
        return new BidResult(false, message, 0, null);
    }

    public static BidResult success(String message, double currentPrice, LocalDateTime endTime) {
        return new BidResult(true, message, currentPrice, endTime);
    }
}