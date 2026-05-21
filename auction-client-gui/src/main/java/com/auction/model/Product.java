package com.auction.model;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;
    private String description;
    private String imagePath;
    private double startPrice;
    private double currentPrice;
    private String status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Product(int id, String name,String description, double startPrice, double currentPrice, String status) {
        this.id = id;
        this.name = name;
        this.description=description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
