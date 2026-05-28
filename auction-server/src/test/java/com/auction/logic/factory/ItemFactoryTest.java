package com.auction.logic.factory;

import com.auction.logic.model.Art;
import com.auction.logic.model.Electronics;
import com.auction.logic.model.Item;
import com.auction.logic.model.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemFactoryTest {

    @Test
    void createArtItemSuccessfully() {
        Item item = ItemFactory.create("art", "Painting", 1000);

        assertInstanceOf(Art.class, item);
        assertEquals("Painting", item.getName());
        assertEquals(1000, item.getStartingPrice());
    }

    @Test
    void createElectronicsItemSuccessfully() {
        Item item = ItemFactory.create("electronics", "Laptop", 2000);

        assertInstanceOf(Electronics.class, item);
        assertEquals("Laptop", item.getName());
        assertEquals(2000, item.getStartingPrice());
    }

    @Test
    void createVehicleItemSuccessfully() {
        Item item = ItemFactory.create("vehicle", "Bike", 500);

        assertInstanceOf(Vehicle.class, item);
        assertEquals("Bike", item.getName());
        assertEquals(500, item.getStartingPrice());
    }

    @Test
    void trimsItemNameAndAcceptsCaseInsensitiveType() {
        Item item = ItemFactory.create(" ART ", "  Painting  ", 1000);

        assertInstanceOf(Art.class, item);
        assertEquals("Painting", item.getName());
    }

    @Test
    void rejectInvalidPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ItemFactory.create("art", "Painting", 0)
        );
    }

    @Test
    void rejectUnknownType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ItemFactory.create("book", "Novel", 100)
        );
    }
}
