package com.auction.server.service;

import com.auction.server.domain.model.Auction;
import com.auction.server.domain.model.BidTransaction;
import com.auction.server.domain.observer.AuctionObserver;

final class AuctionBidObserver implements AuctionObserver {

    @Override
    public void onBidUpdate(Auction auction, BidTransaction bid) {
        System.out.println(
                "[DOMAIN BID] "
                        + bid.getBidder().getUsername()
                        + " placed "
                        + bid.getBidPrice()
                        + " on "
                        + auction.getItemName()
        );
    }
}
