package com.auction.server.domain.model;

import com.auction.server.domain.exception.AuctionClosedException;
import com.auction.server.domain.exception.InvalidBidException;
import com.auction.common.AuctionTime;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionTest {

    @Test
    void auctionUsesItemStartingPriceAndOpensWhenStartTimePassed() {
        Auction auction = newAuction();

        assertEquals(1000, auction.getCurrentPrice());
        assertEquals(AuctionStatus.OPENING, auction.getStatus());
        assertFalse(auction.hasWinner());
    }

    @Test
    void startOpensAuction() throws Exception {
        Auction auction = newAuction();

        auction.start();

        assertEquals(AuctionStatus.OPENING, auction.getStatus());
    }

    @Test
    void bidderCanPlaceValidBid() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        BidTransaction bid = auction.placeBid(bidder, 1200);

        assertEquals(1200, auction.getCurrentPrice());
        assertEquals(1200, bid.getBidPrice());
        assertEquals("bidder1", auction.getHighestBidderUsername());
        assertTrue(auction.hasWinner());
        assertEquals(1, auction.getBids().size());
    }

    @Test
    void sellerCannotBidOwnAuction() throws Exception {
        Seller seller = new Seller("seller1");
        Auction auction = new Auction(
                new Art("Painting", 1000),
                seller,
                AuctionTime.now().minusMinutes(1),
                AuctionTime.now().plusMinutes(5)
        );

        assertThrows(InvalidBidException.class, () -> auction.placeBid(seller, 1200));
    }

    @Test
    void rejectBidLowerThanCurrentPrice() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        assertThrows(InvalidBidException.class, () -> auction.placeBid(bidder, 900));
    }

    @Test
    void cannotBidBeforeAuctionStarts() {
        Auction auction = new Auction(
                new Art("Painting", 1000),
                new Seller("seller1"),
                AuctionTime.now().plusMinutes(1),
                AuctionTime.now().plusMinutes(5)
        );
        Bidder bidder = new Bidder("bidder1");

        assertThrows(AuctionClosedException.class, () -> auction.placeBid(bidder, 1200));
    }

    @Test
    void restoredCurrentPriceIsUsedWhenValidatingBid() {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        auction.restoreCurrentPrice(1500);

        assertThrows(InvalidBidException.class, () -> auction.placeBid(bidder, 1400));
    }

    @Test
    void cannotBidAfterAuctionEnds() {
        Auction auction = new Auction(
                new Art("Painting", 1000),
                new Seller("seller1"),
                AuctionTime.now().minusMinutes(5),
                AuctionTime.now().minusMinutes(1)
        );
        Bidder bidder = new Bidder("bidder1");

        assertThrows(AuctionClosedException.class, () -> auction.placeBid(bidder, 1200));
    }

    @Test
    void manuallyClosedUpcomingAuctionCanBeHydrated() {
        Auction auction = new Auction(
                new Art("Painting", 1000),
                new Seller("seller1"),
                AuctionTime.now().plusMinutes(5),
                AuctionTime.now().minusMinutes(1)
        );
        Bidder bidder = new Bidder("bidder1");

        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertThrows(AuctionClosedException.class, () -> auction.placeBid(bidder, 1200));
    }

    @Test
    void finishReturnsWinnerAndMarksAuctionFinished() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        auction.placeBid(bidder, 1200);

        assertEquals("bidder1", auction.finish());
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }

    @Test
    void notifiesObserverWhenBidIsAccepted() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");
        AtomicReference<BidTransaction> observedBid = new AtomicReference<>();

        auction.addObserver((updatedAuction, bid) -> {
            assertEquals(auction, updatedAuction);
            observedBid.set(bid);
        });

        auction.placeBid(bidder, 1200);

        assertEquals(1200, observedBid.get().getBidPrice());
        assertEquals(bidder, observedBid.get().getBidder());
    }

    private Auction newAuction() {
        return new Auction(
                new Art("Painting", 1000),
                new Seller("seller1"),
                AuctionTime.now().minusMinutes(1),
                AuctionTime.now().plusMinutes(5)
        );
    }
}
