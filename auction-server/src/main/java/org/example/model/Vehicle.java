package org.example.model;

public class Vehicle extends Item {
    public Vehicle(int id, String name, String description, String imagePath,
                   double startPrice, double currentPrice) {
        super(id, name, description, imagePath, startPrice, currentPrice);
    }

    @Override
    public String printInfo() {
        return "Vehicle: " + getName();
    }
}
