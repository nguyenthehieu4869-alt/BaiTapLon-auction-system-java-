package com.auction.server.service;

import com.auction.server.domain.exception.AuctionClosedException;
import com.auction.server.domain.model.Auction;
import com.auction.server.domain.model.AuctionStatus;
import com.auction.server.domain.model.Bidder;
import com.auction.common.AuctionTime;
import com.auction.common.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainAuctionMapperTest {

    private final DomainAuctionMapper mapper = new DomainAuctionMapper();

    @Test
    void restoresCurrentPriceFromDatabaseSnapshot() {
        Auction auction = mapper.toDomainAuction(snapshot(ProductStatus.OPENING, 1500));

        assertEquals(1500, auction.getCurrentPrice());
        assertEquals(AuctionStatus.OPENING, auction.getStatus());
    }

    @Test
    void preservesStoredFinishedStatusBeforeEndTime() {
        Auction auction = mapper.toDomainAuction(snapshot(ProductStatus.FINISHED, 1000));

        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertThrows(
                AuctionClosedException.class,
                () -> auction.placeBid(new Bidder("bidder1"), 1200)
        );
    }

    @Test
    void supportsLegacySnapshotWithoutSchedule() {
        Auction auction = mapper.toDomainAuction(new BidService.ProductSnapshot(
                "Painting",
                1000,
                1000,
                ProductStatus.OPENING,
                null,
                null,
                "seller1"
        ));

        assertEquals(AuctionStatus.OPENING, auction.getStatus());
    }

    private BidService.ProductSnapshot snapshot(String status, double currentPrice) {
        return new BidService.ProductSnapshot(
                "Painting",
                1000,
                currentPrice,
                status,
                AuctionTime.now().minusMinutes(1),
                AuctionTime.now().plusMinutes(5),
                "seller1"
        );
    }
}
