package com.auction.service;

public class BidResult {
    private final boolean success;
    private final String message;

    public BidResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static BidResult success(String message) {
        return new BidResult(true, message);
    }

    public static BidResult failure(String message) {
        return new BidResult(false, message);
    }
}
