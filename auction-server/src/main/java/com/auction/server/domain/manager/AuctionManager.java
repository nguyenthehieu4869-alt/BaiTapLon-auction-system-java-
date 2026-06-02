package com.auction.server.domain.manager;

import com.auction.server.domain.model.Auction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager {

    private static AuctionManager instance;
    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên đấu giá");
        }

        auctions.put(auction.getId(), auction);
    }

    public Auction getAuction(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        return auctions.get(id);
    }

    public void removeAuction(String id) {
        if (id == null || id.isBlank()) {
            return;
        }

        auctions.remove(id);
    }
}