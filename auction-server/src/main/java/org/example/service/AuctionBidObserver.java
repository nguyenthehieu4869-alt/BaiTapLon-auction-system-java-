package org.example.service;

import com.auction.logic.model.Auction;
import com.auction.logic.model.BidTransaction;
import com.auction.logic.observer.AuctionObserver;

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
