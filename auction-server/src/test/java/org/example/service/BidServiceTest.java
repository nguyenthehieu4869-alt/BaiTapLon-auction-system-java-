package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BidServiceTest {
    private final BidService bidService = new BidService();

    @Test
    void rejectsBlankBidderBeforeDatabaseAccess() {
        BidResult result = bidService.placeBid(1, " ", 1000);

        assertFalse(result.isSuccess());
    }

    @Test
    void rejectsInvalidBidPriceBeforeDatabaseAccess() {
        BidResult result = bidService.placeBid(1, "bidder1", 0);

        assertFalse(result.isSuccess());
    }
}
