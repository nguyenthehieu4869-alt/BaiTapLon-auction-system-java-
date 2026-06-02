package com.auction.network;

import com.auction.common.network.protocol.Message;

public interface BidUpdateListener {
    void onBidUpdate(Message message);
}