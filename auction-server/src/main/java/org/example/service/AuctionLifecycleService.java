package org.example.service;

import org.example.database.ProductDAO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionLifecycleService {
    private final ProductDAO productDAO;
    private final ScheduledExecutorService scheduler;

    public AuctionLifecycleService() {
        this(new ProductDAO());
    }

    public AuctionLifecycleService(ProductDAO productDAO) {
        this.productDAO = productDAO;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "auction-lifecycle");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                productDAO::refreshAuctionStatuses,
                0,
                5,
                TimeUnit.SECONDS
        );
    }
}
