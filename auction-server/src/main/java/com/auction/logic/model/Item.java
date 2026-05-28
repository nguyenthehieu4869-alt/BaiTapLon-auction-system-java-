package com.auction.logic.model;

public abstract class Item extends Entity {
    protected String name;
    protected double startingPrice;

    public Item(String name, double price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }

        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Starting price must be positive");
        }

        this.name = name.trim();
        this.startingPrice = price;
    }

    public String getName() {
        return name;
    }

    public double getStartingPrice() {
        return startingPrice;
    }
}