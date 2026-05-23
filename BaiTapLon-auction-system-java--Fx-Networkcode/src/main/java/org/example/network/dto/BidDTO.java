package org.example.network.dto;

public class BidDTO {
    private int id;
    private int productId;
    private String bidderUsername;
    private double bidPrice;
    private String bidTime;

    public BidDTO(int id, int productId, String bidderUsername, double bidPrice, String bidTime) {
        this.id = id;
        this.productId = productId;
        this.bidderUsername = bidderUsername;
        this.bidPrice = bidPrice;
        this.bidTime = bidTime;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getBidderUsername() { return bidderUsername; }
    public double getBidPrice() { return bidPrice; }
    public String getBidTime() { return bidTime; }
}