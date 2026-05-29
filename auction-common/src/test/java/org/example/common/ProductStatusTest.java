package org.example.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductStatusTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 29, 12, 0);

    @Test
    void currentReturnsComingSoonWhenStartTimeIsInFuture() {
        String status = ProductStatus.current(
                NOW.plusMinutes(10),
                NOW.plusHours(1),
                ProductStatus.OPENING,
                NOW
        );

        assertEquals(ProductStatus.COMING_SOON, status);
    }

    @Test
    void currentReturnsFinishedWhenEndTimeHasPassed() {
        String status = ProductStatus.current(
                NOW.minusHours(2),
                NOW.minusMinutes(1),
                ProductStatus.OPENING,
                NOW
        );

        assertEquals(ProductStatus.FINISHED, status);
    }

    @Test
    void currentPreservesFinishedStatus() {
        String status = ProductStatus.current(
                NOW.minusMinutes(10),
                NOW.plusHours(1),
                ProductStatus.FINISHED,
                NOW
        );

        assertEquals(ProductStatus.FINISHED, status);
    }

    @Test
    void currentReturnsOpeningForActiveAuction() {
        String status = ProductStatus.current(
                NOW.minusMinutes(10),
                NOW.plusHours(1),
                ProductStatus.COMING_SOON,
                NOW
        );

        assertEquals(ProductStatus.OPENING, status);
    }

    @Test
    void currentAcceptsLegacyOpenStatus() {
        String status = ProductStatus.current(
                NOW.minusMinutes(10),
                NOW.plusHours(1),
                "OPEN",
                NOW
        );

        assertEquals(ProductStatus.OPENING, status);
    }

    @Test
    void currentAcceptsMissingStatusAndUsesTimes() {
        String status = ProductStatus.current(
                NOW.minusMinutes(10),
                NOW.plusHours(1),
                null,
                NOW
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
