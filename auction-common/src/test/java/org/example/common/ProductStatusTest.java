package org.example.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductStatusTest {

    @Test
    void currentReturnsComingSoonWhenStartTimeIsInFuture() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.plusMinutes(10),
                now.plusHours(1),
                ProductStatus.OPENING
        );

        assertEquals(ProductStatus.COMING_SOON, status);
    }

    @Test
    void currentReturnsFinishedWhenEndTimeHasPassed() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.minusHours(2),
                now.minusMinutes(1),
                ProductStatus.OPENING
        );

        assertEquals(ProductStatus.FINISHED, status);
    }

    @Test
    void currentPreservesFinishedStatus() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.minusMinutes(10),
                now.plusHours(1),
                ProductStatus.FINISHED
        );

        assertEquals(ProductStatus.FINISHED, status);
    }

    @Test
    void currentReturnsOpeningForActiveAuction() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.minusMinutes(10),
                now.plusHours(1),
                ProductStatus.COMING_SOON
        );

        assertEquals(ProductStatus.OPENING, status);
    }

    @Test
    void currentAcceptsLegacyOpenStatus() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.minusMinutes(10),
                now.plusHours(1),
                "OPEN"
        );

        assertEquals(ProductStatus.OPENING, status);
    }

    @Test
    void currentAcceptsMissingStatusAndUsesTimes() {
        LocalDateTime now = LocalDateTime.now();

        String status = ProductStatus.current(
                now.minusMinutes(10),
                now.plusHours(1),
                null
        );

        assertEquals(ProductStatus.OPENING, status);
    }

    @Test
    void normalizeAcceptsLegacyClosedStatus() {
        assertEquals(ProductStatus.FINISHED, ProductStatus.normalize("CLOSED"));
    }

    @Test
    void normalizeRejectsUnknownStatus() {
        assertThrows(IllegalArgumentException.class, () -> ProductStatus.normalize("PAUSED"));
    }
}
