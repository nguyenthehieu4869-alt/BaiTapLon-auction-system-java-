package com.auction.model;

public class Product {
    private int id;
    private String name;
    private String description;
    private double startPrice;
    private double currentPrice;
    private String status;

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

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
}