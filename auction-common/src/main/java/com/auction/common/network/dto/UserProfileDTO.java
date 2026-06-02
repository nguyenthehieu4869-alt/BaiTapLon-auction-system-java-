package com.auction.common.network.dto;

public class UserProfileDTO {
    private String username;
    private String email;
    private int wonAuctionCount;
    private int soldProductCount;
    private double walletBalance;

    public UserProfileDTO(String username, String email, int wonAuctionCount, int soldProductCount, double walletBalance) {
        this.username = username;
        this.email = email;
        this.wonAuctionCount = wonAuctionCount;
        this.soldProductCount = soldProductCount;
        this.walletBalance = walletBalance;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getWonAuctionCount() { return wonAuctionCount; }
    public int getSoldProductCount() { return soldProductCount; }
    public double getWalletBalance() { return walletBalance; }
}
