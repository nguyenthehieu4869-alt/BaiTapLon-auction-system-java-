package org.example.service;

import com.auction.logic.exception.AuctionClosedException;
import com.auction.logic.model.Auction;
import com.auction.logic.model.AuctionStatus;
import com.auction.logic.model.Bidder;
import org.example.common.AuctionTime;
import org.example.common.ProductStatus;
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
