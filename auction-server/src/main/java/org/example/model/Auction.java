package org.example.model;

public class Auction extends Entity {
    private final Product product;
    private final AuctionStatus status;

    public Auction(int id, Product product, AuctionStatus status) {
        super(id);
        this.product = product;
        this.status = status;
    }

    public Product getProduct() {
        return product;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    @Override
    public String printInfo() {
        return "Auction " + getId() + " - " + status;
    }
}
