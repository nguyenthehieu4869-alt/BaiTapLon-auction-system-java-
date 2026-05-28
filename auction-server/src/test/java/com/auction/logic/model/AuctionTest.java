package com.auction.logic.model;

import com.auction.logic.exception.AuctionClosedException;
import com.auction.logic.exception.InvalidBidException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionTest {

    @Test
    void auctionStartsWithItemStartingPriceAndComingSoonStatus() {
        Auction auction = newAuction();

        assertEquals(1000, auction.getCurrentPrice());
        assertEquals(AuctionStatus.COMING_SOON, auction.getStatus());
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

        auction.start();
        auction.placeBid(bidder, 1200);

        assertEquals(1200, auction.getCurrentPrice());
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
                LocalDateTime.now().plusMinutes(5)
        );

        auction.start();

        assertThrows(InvalidBidException.class, () -> auction.placeBid(seller, 1200));
    }

    @Test
    void rejectBidLowerThanCurrentPrice() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        auction.start();

        assertThrows(InvalidBidException.class, () -> auction.placeBid(bidder, 900));
    }

    @Test
    void cannotBidBeforeAuctionStarts() {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        assertThrows(AuctionClosedException.class, () -> auction.placeBid(bidder, 1200));
    }

    @Test
    void finishReturnsWinnerAndMarksAuctionFinished() throws Exception {
        Auction auction = newAuction();
        Bidder bidder = new Bidder("bidder1");

        auction.start();
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

        auction.start();
        auction.placeBid(bidder, 1200);

        assertEquals(1200, observedBid.get().getBidPrice());
        assertEquals(bidder, observedBid.get().getBidder());
    }

    private Auction newAuction() {
        return new Auction(
                new Art("Painting", 1000),
                new Seller("seller1"),
                LocalDateTime.now().plusMinutes(5)
        );
    }
}
