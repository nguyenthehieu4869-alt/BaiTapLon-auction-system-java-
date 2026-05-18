package com.auction.service;

import com.auction.model.Bid;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class BidDAO {
    public boolean addBid(int productId, String bidderUsername, double bidPrice) {
        String sql = "INSERT INTO bids(product_id,bidder_username,bid_price) VALUES(?,?,?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productId);
                stmt.setString(2, bidderUsername);
                stmt.setDouble(3, bidPrice);

                int rows = stmt.executeUpdate();

                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Bid> getBidsByProductId(int productId) {
        ObservableList<Bid> bids = FXCollections.observableArrayList();

        String sql = "SELECT id, product_id, bidder_username, bid_price, bid_time " +
                "FROM bids WHERE product_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("ket noi dtb fail");
                return bids;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Bid bid = new Bid(
                                rs.getInt("id"),
                                rs.getInt("product_id"),
                                rs.getString("bidder_username"),
                                rs.getDouble("bid_price"),
                                rs.getTimestamp("bid_time")
                        );

                        bids.add(bid);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bids;

    }

    public String getWinnerUsernameByProductId(int productId) {
        String sql = "SELECT bidder_username FROM bids WHERE product_id = ? ORDER BY bid_price DESC, bid_time ASC LIMIT 1";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("bidder_username");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
