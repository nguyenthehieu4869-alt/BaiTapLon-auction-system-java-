package org.example.model;

public abstract class Item extends Entity {
    private final String name;
    private final String description;
    private final String imagePath;
    private final double startPrice;
    private final double currentPrice;

    protected Item(int id, String name, String description, String imagePath,
                   double startPrice, double currentPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
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
}
