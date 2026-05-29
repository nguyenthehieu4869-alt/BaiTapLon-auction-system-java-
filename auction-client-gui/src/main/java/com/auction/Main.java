package com.auction;

import javafx.application.Application;
import org.example.common.AuctionTime;

public class Main {
    public static void main(String[] args) {
        AuctionTime.installAsDefaultTimeZone();
        Application.launch(AuctionApplication.class, args);
    }
}
