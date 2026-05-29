package org.example.model;

public class BidTransaction extends Entity {
    private final int userId;
    private final int productId;
    private final String bidderUsername;
    private final double amount;

    public BidTransaction(int id, int userId, int productId, String bidderUsername, double amount) {
        super(id);
        this.userId = userId;
        this.productId = productId;
        this.bidderUsername = bidderUsername;
        this.amount = amount;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String printInfo() {
        return "Bid " + amount + " for product " + productId;
    }
}
