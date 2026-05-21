package com.auction.service;

import com.auction.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BidService {
    private static final int EXTENSION_WINDOW_SECONDS = 15;
    private static final int EXTENSION_SECONDS = 15;

    public BidResult placeBid(Product product, String username, double newPrice) {
        if (product == null) {
            return BidResult.failure("Không tìm thấy sản phẩm");
        }

        if (username == null || username.trim().isEmpty()) {
            return BidResult.failure("Không xác định được tài khoản bidder. Vui lòng đăng nhập lại.");
        }

        if (!Double.isFinite(newPrice) || newPrice <= 0) {
            return BidResult.failure("Giá đặt không hợp lệ");
        }

        String bidder = username.trim();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return BidResult.failure("Không kết nối được cơ sở dữ liệu");
            }

            conn.setAutoCommit(false);

            ProductSnapshot latestProduct = lockProduct(conn, product.getId());
            if (latestProduct == null) {
                rollbackQuietly(conn);
                return BidResult.failure("Không tìm thấy sản phẩm");
            }

            if (isAuctionClosed(latestProduct.status(), latestProduct.endTime())) {
                rollbackQuietly(conn);
                return BidResult.failure("Phiên đấu giá đã kết thúc, không thể đặt giá.");
            }

            if (newPrice <= latestProduct.currentPrice()) {
                rollbackQuietly(conn);
                return BidResult.failure("Giá đặt phải cao hơn giá hiện tại");
            }

            LocalDateTime extendedEndTime = extendIfNeeded(latestProduct.endTime());

            updateCurrentPrice(conn, product.getId(), newPrice);
            if (extendedEndTime != null) {
                updateEndTime(conn, product.getId(), extendedEndTime);
            }
            insertBid(conn, product.getId(), bidder, newPrice);

            conn.commit();
            product.setCurrentPrice(newPrice);
            product.setStatus(latestProduct.status());
            if (extendedEndTime != null) {
                product.setEndTime(extendedEndTime);
            }

            return BidResult.success("Đặt giá thành công");
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return BidResult.failure("Đặt giá thất bại");
        } finally {
            closeQuietly(conn);
        }
    }

    private ProductSnapshot lockProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT current_price, status, end_time FROM products WHERE id = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Timestamp endTimestamp = rs.getTimestamp("end_time");
                LocalDateTime endTime = endTimestamp == null ? null : endTimestamp.toLocalDateTime();

                return new ProductSnapshot(
                        rs.getDouble("current_price"),
                        rs.getString("status"),
                        endTime
                );
            }
        }
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

        LocalDateTime now = LocalDateTime.now();
        long secondsLeft = java.time.Duration.between(now, endTime).getSeconds();

        if (secondsLeft > 0 && secondsLeft <= EXTENSION_WINDOW_SECONDS) {
            return endTime.plusSeconds(EXTENSION_SECONDS);
        }

        return null;
    }

    private void updateCurrentPrice(Connection conn, int productId, double newPrice) throws SQLException {
        String sql = "UPDATE products SET current_price = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    private void updateEndTime(Connection conn, int productId, LocalDateTime endTime) throws SQLException {
        String sql = "UPDATE products SET end_time = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(endTime));
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    private void insertBid(Connection conn, int productId, String bidderUsername, double bidPrice) throws SQLException {
        String sql = "INSERT INTO bids(product_id, bidder_username, bid_price) VALUES(?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setString(2, bidderUsername);
            stmt.setDouble(3, bidPrice);
            stmt.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private record ProductSnapshot(double currentPrice, String status, LocalDateTime endTime) {
    }
}
