package org.example.network.dto;

public class ProductSaveRequest {
    private int id;
    private String name;
    private String description;
    private String imagePath;
    private double startPrice;
    private String status;
    private String startTime;
    private String endTime;
    private String sellerUsername;

    public ProductSaveRequest(int id, String name, String description, String imagePath,
                              double startPrice, String status, String startTime,
                              String endTime, String sellerUsername) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.startPrice = startPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerUsername = sellerUsername;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public double getStartPrice() { return startPrice; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getSellerUsername() { return sellerUsername; }
}
