package com.auction.logic.model;

import com.auction.logic.exception.InvalidBidException;
import com.auction.logic.exception.AuctionClosedException;
import com.auction.logic.observer.AuctionObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {

    private Item item;
    private Seller seller;

    private double currentPrice;
    private User highestBidder;

    private final List<BidTransaction> bids = new ArrayList<>();
    private List<AuctionObserver> observers = new ArrayList<>();

    private LocalDateTime endTime;

    private AuctionStatus status = AuctionStatus.COMING_SOON;

    private final ReentrantLock lock = new ReentrantLock();

    public Auction(Item item, Seller seller, LocalDateTime endTime) {
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm");
        }

        if (seller == null) {
            throw new IllegalArgumentException("Không xác định seller account");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("Vui lòng nhập thời điểm kết thúc !");
        }

        if (!endTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm hiện tại");
        }

        this.item = item;
        this.seller = seller;
        this.currentPrice = item.getStartingPrice();
        this.endTime = endTime;
    }

    public void start() throws AuctionClosedException {
        if (status == AuctionStatus.FINISHED) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc");
        }

        if (isEnded()) {
            finish();
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc");
        }

        status = AuctionStatus.OPENING;
    }

    public void addObserver(AuctionObserver obs) {
        if (obs == null) {
            throw new IllegalArgumentException("Observer is null");
        }

        if (!observers.contains(obs)) {
            observers.add(obs);
        }
    }

    private void notifyObservers(BidTransaction bid) {
        for (AuctionObserver o : observers) {
            o.onBidUpdate(this, bid);
        }
    }

    public void placeBid(User bidder, double bidPrice)
            throws InvalidBidException, AuctionClosedException {

        lock.lock();
        try {
            if (bidder == null) {
                throw new InvalidBidException("Không xác định được bidder");
            }

            if (seller.getUsername().equalsIgnoreCase(bidder.getUsername())) {
                throw new InvalidBidException("Seller không thể đặt giá sản phẩm của chính mình");
            }

            validateAuctionState();

            if (isEnded()) {
                finish();
                throw new AuctionClosedException("Phiên đấu giá đã kết thúc");
            }

            validateBid(bidPrice);

            BidTransaction bid = new BidTransaction(bidder, bidPrice);
            bids.add(bid);

            currentPrice = bidPrice;
            highestBidder = bidder;

            notifyObservers(bid);

        } finally {
            lock.unlock();
        }
    }

    private void validateAuctionState() throws AuctionClosedException {
        if (status == AuctionStatus.FINISHED) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc");
        }

        if (status == AuctionStatus.COMING_SOON) {
            throw new AuctionClosedException("Phiên đấu giá chưa bắt đầu");
        }
    }

    private void validateBid(double bidPrice) throws InvalidBidException {
        if (!Double.isFinite(bidPrice) || bidPrice <= 0) {
            throw new InvalidBidException("Giá đặt không hợp lệ");
        }

        if (bidPrice <= currentPrice) {
            throw new InvalidBidException("Giá đặt phải cao hơn giá hiện tại");
        }
    }

    private boolean isEnded() {
        return !endTime.isAfter(LocalDateTime.now());
    }

    public String finish() {
        if (status == AuctionStatus.FINISHED) {
            return getHighestBidderUsername();
        }

        status = AuctionStatus.FINISHED;

        return getHighestBidderUsername();
    }

    public String getHighestBidderUsername() {
        return highestBidder == null ? null : highestBidder.getUsername();
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public String getWinnerName() {
        return highestBidder != null ? highestBidder.getUsername() : "None";
    }

    public boolean hasWinner() {
        return highestBidder != null;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public Item getItem() {
        return item;
    }

    public String getItemName() {
        return item.getName();
    }

    public Seller getSeller() {
        return seller;
    }

    public String getSellerUsername() {
        return seller.getUsername();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<BidTransaction> getBids() {
        return new ArrayList<>(bids);
    }
}
