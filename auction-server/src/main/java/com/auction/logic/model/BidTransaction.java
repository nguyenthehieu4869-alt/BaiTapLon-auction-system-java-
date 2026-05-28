package com.auction.logic.model;

import java.time.LocalDateTime;

public class BidTransaction {
    private final User bidder;
    private final double bidPrice;
    private final LocalDateTime time;

    public BidTransaction(User bidder, double bidPrice) {
        if (bidder == null) {
            throw new IllegalArgumentException("Không xác định được bidder");
        }

        if (!Double.isFinite(bidPrice) || bidPrice <= 0) {
            throw new IllegalArgumentException( "Giá đặt không hợp lệ");
        }

        this.bidder = bidder;
        this.bidPrice = bidPrice;
        this.time = LocalDateTime.now();
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public User getBidder() {
        return bidder;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
