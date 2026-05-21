package org.example.database;

import java.sql.*;

public class BidDAO {

    public boolean placeBid(int userId, int productId, double amount) {
        try {
            Connection conn = DatabaseManager.getConnection();

            String sql = "INSERT INTO bids (user_id, product_id, amount) VALUES (?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setDouble(3, amount);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getHighestBid(int productId) {
        try {
            Connection conn = DatabaseManager.getConnection();

            String sql = "SELECT MAX(amount) FROM bids WHERE product_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}