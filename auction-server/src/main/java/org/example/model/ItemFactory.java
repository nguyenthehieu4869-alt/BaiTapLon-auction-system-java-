package org.example.model;

public final class ItemFactory {
    private ItemFactory() {
    }

    public static Item create(String type, int id, String name, String description,
                              String imagePath, double startPrice, double currentPrice) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase();

        return switch (normalizedType) {
            case "ART" -> new Art(id, name, description, imagePath, startPrice, currentPrice);
            case "VEHICLE" -> new Vehicle(id, name, description, imagePath, startPrice, currentPrice);
            default -> new Electronics(id, name, description, imagePath, startPrice, currentPrice);
        };
    }
}
