package com.auction.network;

import org.example.network.protocol.Message;

public interface ProductUpdateListener {
    void onProductChanged(Message message);
}
