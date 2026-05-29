package org.example.model;

public class Bid extends BidTransaction {

    public Bid(int userId, int productId, double amount) {
        super(0, userId, productId, null, amount);
    }
}
