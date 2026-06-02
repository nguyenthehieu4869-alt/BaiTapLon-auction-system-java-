package com.auction.server.domain.model;

import com.auction.server.domain.manager.AuctionManager;

public class Admin extends User {

    public Admin(String username) {

        super(
                username,
                UserRole.ADMIN
        );
    }

    @Override
    public void onBidUpdate(
            Auction auction,
            BidTransaction bid
    ) {

        System.out.println(
                "[ADMIN] monitoring auction: "
                        + bid.getBidPrice()
        );
    }

    // ===== FORCE FINISH =====
    public String forceFinishAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên đấu giá");
        }

        String winnerUsername = auction.finish();

        System.out.println(
                "Admin đã đóng phiên đấu giá."
        );

        return winnerUsername;
    }

    // ===== DELETE AUCTION =====
    public void deleteAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction is null");
        }

        AuctionManager.getInstance().removeAuction(auction.getId());

        System.out.println(
                "Admin đã xoá phiên đấu giá."
        );
    }

    // ===== BAN USER =====
    public void banUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        System.out.println(
                user.getUsername()
                        + " đã bị khoá tài khoản."
        );
    }
}
