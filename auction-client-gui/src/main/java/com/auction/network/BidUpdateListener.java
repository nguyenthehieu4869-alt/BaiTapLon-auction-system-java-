package com.auction.network;

import org.example.network.protocol.Message;

public interface BidUpdateListener {
    void onBidUpdate(Message message);
}