package com.auction.network;

import com.auction.common.network.protocol.Message;

public interface ProductUpdateListener {
    void onProductChanged(Message message);
}
