package org.example.database;

import org.example.common.AuctionTime;
import org.example.common.ProductStatus;
import org.example.network.dto.BidDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    public void updateCurrentPrice(Connection conn, int productId, double bidPrice) throws SQLException {
        String sql = "UPDATE products SET current_price = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, bidPrice);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public void updateEndTime(Connection conn, int productId, LocalDateTime endTime) throws SQLException {
        String sql = "UPDATE products SET end_time = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, endTime);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public void insertBid(Connection conn, int productId, String bidderUsername, double bidPrice) throws SQLException {
        String sql = "INSERT INTO bids(product_id, bidder_username, bid_price) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, bidderUsername);
            ps.setDouble(3, bidPrice);
            ps.executeUpdate();
        }
    }

    public List<BidDTO> getBidsByProductId(int productId) {
        List<BidDTO> bids = new ArrayList<>();

        String sql = """
            SELECT id, product_id, bidder_username, bid_price, bid_time
            FROM bids
            WHERE product_id = ?
            ORDER BY bid_time DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp bidTime = rs.getTimestamp("bid_time");

                    bids.add(new BidDTO(
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            rs.getString("bidder_username"),
                            rs.getDouble("bid_price"),
                            bidTime == null ? null : bidTime.toLocalDateTime().toString()
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bids;
    }

    public String getWinnerUsernameByProductId(int productId) {
        String sql = """
            SELECT bidder_username
            FROM bids
            WHERE product_id = ?
            ORDER BY bid_price DESC, bid_time ASC
            LIMIT 1
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bidder_username");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int countWonAuctionsByBidder(String bidderUsername) {
        String sql = """
            SELECT COUNT(*)
            FROM products p
            WHERE (p.status = ? OR p.end_time <= ?)
              AND (
                  SELECT b.bidder_username
                  FROM bids b
                  WHERE b.product_id = p.id
                  ORDER BY b.bid_price DESC, b.bid_time ASC, b.id ASC
                  LIMIT 1
              ) = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ProductStatus.FINISHED);
            ps.setObject(2, AuctionTime.now());
            ps.setString(3, bidderUsername);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
