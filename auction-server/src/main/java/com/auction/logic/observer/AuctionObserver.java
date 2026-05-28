package com.auction.logic.observer;

import com.auction.logic.model.Auction;
import com.auction.logic.model.BidTransaction;

public interface AuctionObserver {
    void onBidUpdate(Auction auction, BidTransaction bid);
}