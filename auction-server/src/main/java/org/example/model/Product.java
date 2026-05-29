package org.example.model;

public class Product extends Item {
    private final String status;
    private final String startTime;
    private final String endTime;
    private final String sellerUsername;

    public Product(int id, String name, String description, String imagePath,
                   double startPrice, double currentPrice, String status,
                   String startTime, String endTime, String sellerUsername) {
        super(id, name, description, imagePath, startPrice, currentPrice);
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerUsername = sellerUsername;
    }

    public String getStatus() {
        return status;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    @Override
    public String printInfo() {
        return getName() + " - current price: " + getCurrentPrice();
    }
}
