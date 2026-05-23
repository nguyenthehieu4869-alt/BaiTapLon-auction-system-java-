package org.example.service;

import org.example.database.BidDAO;
import org.example.database.DatabaseManager;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class BidService {
    private static final int EXTENSION_WINDOW_SECONDS = 15;
    private static final int EXTENSION_SECONDS = 15;

    private final BidDAO bidDAO = new BidDAO();

    public BidResult placeBid(int productId, String bidderUsername, double bidPrice) {
        if (bidderUsername == null || bidderUsername.isBlank()) {
            return BidResult.failure("Không xác định được bidder");
        }

        if (!Double.isFinite(bidPrice) || bidPrice <= 0) {
            return BidResult.failure("Giá đặt không hợp lệ");
        }

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            ProductSnapshot product = lockProduct(conn, productId);

            if (product == null) {
                conn.rollback();
                return BidResult.failure("Không tìm thấy sản phẩm");
            }

            if (isAuctionClosed(product.status, product.endTime)) {
                conn.rollback();
                return BidResult.failure("Phiên đấu giá đã kết thúc");
            }

            if (bidPrice <= product.currentPrice) {
                conn.rollback();
                return BidResult.failure("Giá đặt phải cao hơn giá hiện tại");
            }

            LocalDateTime newEndTime = extendIfNeeded(product.endTime);

            bidDAO.updateCurrentPrice(conn, productId, bidPrice);

            if (newEndTime != null) {
                bidDAO.updateEndTime(conn, productId, Timestamp.valueOf(newEndTime));
            } else {
                newEndTime = product.endTime;
            }

            bidDAO.insertBid(conn, productId, bidderUsername.trim(), bidPrice);

            conn.commit();

            return BidResult.success("Đặt giá thành công", bidPrice, newEndTime);

        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return BidResult.failure("Đặt giá thất bại");
        } finally {
            closeQuietly(conn);
        }
    }

    private ProductSnapshot lockProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT current_price, status, end_time FROM products WHERE id = ? FOR UPDATE";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, productId);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            return null;
        }

        Timestamp endTimestamp = rs.getTimestamp("end_time");

        return new ProductSnapshot(
                rs.getDouble("current_price"),
                rs.getString("status"),
                endTimestamp == null ? null : endTimestamp.toLocalDateTime()
        );
    }

    private boolean isAuctionClosed(String status, LocalDateTime endTime) {
        if ("CLOSED".equalsIgnoreCase(status)
                || "FINISHED".equalsIgnoreCase(status)
                || "CANCELED".equalsIgnoreCase(status)
                || "DELETED".equalsIgnoreCase(status)) {
            return true;
        }

        return endTime != null && !endTime.isAfter(LocalDateTime.now());
    }

    private LocalDateTime extendIfNeeded(LocalDateTime endTime) {
        if (endTime == null) {
            return null;
        }

        long secondsLeft = Duration.between(LocalDateTime.now(), endTime).getSeconds();

        if (secondsLeft > 0 && secondsLeft <= EXTENSION_WINDOW_SECONDS) {
            return endTime.plusSeconds(EXTENSION_SECONDS);
        }

        return null;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) return;

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) return;

        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private static class ProductSnapshot {
        double currentPrice;
        String status;
        LocalDateTime endTime;

        ProductSnapshot(double currentPrice, String status, LocalDateTime endTime) {
            this.currentPrice = currentPrice;
            this.status = status;
            this.endTime = endTime;
        }
    }
}
