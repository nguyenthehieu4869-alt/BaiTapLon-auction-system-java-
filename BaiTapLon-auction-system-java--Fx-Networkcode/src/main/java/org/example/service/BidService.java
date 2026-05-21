package org.example.service;

import org.example.database.BidDAO;

public class BidService {

    private BidDAO dao = new BidDAO();

    public boolean placeBid(int userId, int productId, double amount) {

        double current = dao.getHighestBid(productId);

        if (amount <= current) {
            System.out.println("❌ Giá phải cao hơn giá hiện tại");
            return false;
        }

        return dao.placeBid(userId, productId, amount);
    }

    public double getHighestBid(int productId) {
        return dao.getHighestBid(productId);
    }
}

