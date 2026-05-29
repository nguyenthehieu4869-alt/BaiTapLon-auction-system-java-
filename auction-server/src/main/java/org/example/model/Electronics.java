package org.example.model;

public class Electronics extends Item {
    public Electronics(int id, String name, String description, String imagePath,
                       double startPrice, double currentPrice) {
        super(id, name, description, imagePath, startPrice, currentPrice);
    }

    @Override
    public String printInfo() {
        return "Electronics: " + getName();
    }
}
