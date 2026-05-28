package com.auction.logic.model;

public class Bidder extends User {

    public Bidder(String username) {
        super(username, UserRole.BIDDER);
    }

    @Override
    public void onBidUpdate(
            Auction auction,
            BidTransaction bid
    ) {

        if (bid.getBidder() == this) {

            System.out.println(
                    "[" + username + "] "
                            + "Bạn đang là người trả giá cao nhất."
            );

        } else {

            System.out.println(
                    "[" + username + "] "
                            + "Bạn đã bị người khác vượt."
            );
        }
    }
}