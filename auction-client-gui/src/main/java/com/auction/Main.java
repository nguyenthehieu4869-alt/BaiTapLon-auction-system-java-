package com.auction;

import javafx.application.Application;
import com.auction.common.AuctionTime;

public class Main {
    public static void main(String[] args) {
        AuctionTime.installAsDefaultTimeZone();
        Application.launch(AuctionApplication.class, args);
    }
}
