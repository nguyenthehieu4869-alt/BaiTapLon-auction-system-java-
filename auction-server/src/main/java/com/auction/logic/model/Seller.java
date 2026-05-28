package com.auction.logic.model;


public class Seller extends User {

    public Seller(String username) {
        super(username, UserRole.SELLER);
    }

    @Override
    public void onBidUpdate(
            Auction auction,
            BidTransaction bid
    ) {

        System.out.println(
                "[SELLER " + username + "] "
                        + bid.getBidder().getUsername()
                        + " đã đặt giá "
                        + bid.getBidPrice()
        );
    }
}
