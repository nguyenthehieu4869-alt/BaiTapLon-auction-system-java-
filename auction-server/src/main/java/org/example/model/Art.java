package org.example.model;

public class Art extends Item {
    public Art(int id, String name, String description, String imagePath,
               double startPrice, double currentPrice) {
        super(id, name, description, imagePath, startPrice, currentPrice);
    }

    @Override
    public String printInfo() {
        return "Art: " + getName();
    }
}
