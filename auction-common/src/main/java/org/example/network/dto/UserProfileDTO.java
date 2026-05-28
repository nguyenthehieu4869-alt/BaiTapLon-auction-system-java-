package org.example.network.dto;

public class UserProfileDTO {
    private String username;
    private String email;
    private int wonAuctionCount;
    private int soldProductCount;

    public UserProfileDTO(String username, String email, int wonAuctionCount, int soldProductCount) {
        this.username = username;
        this.email = email;
        this.wonAuctionCount = wonAuctionCount;
        this.soldProductCount = soldProductCount;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getWonAuctionCount() { return wonAuctionCount; }
    public int getSoldProductCount() { return soldProductCount; }
}
