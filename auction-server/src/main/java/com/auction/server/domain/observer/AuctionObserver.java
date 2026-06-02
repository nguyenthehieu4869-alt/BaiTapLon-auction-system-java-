package com.auction.server.domain.observer;

import com.auction.server.domain.model.Auction;
import com.auction.server.domain.model.BidTransaction;

public interface AuctionObserver {
    void onBidUpdate(Auction auction, BidTransaction bid);
}