package org.example.service;

import org.example.common.AuctionTime;
import org.example.common.ProductStatus;
import org.example.database.BidDAO;
import org.example.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String normalizedBidderUsername = bidderUsername.trim();

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

            if (product.sellerUsername != null
                    && product.sellerUsername.equalsIgnoreCase(normalizedBidderUsername)) {
                conn.rollback();
                return BidResult.failure("Người bán không thể đặt giá sản phẩm của chính mình !");
            }

            LocalDateTime now = AuctionTime.now();

            if (isAuctionFinished(product.status, product.endTime, now)) {
                conn.rollback();
                return BidResult.failure("Phiên đấu giá đã kết thúc");
            }

            if (isAuctionNotStarted(product.startTime, now)) {
                conn.rollback();
                return BidResult.failure("Phiên đấu giá chưa bắt đầu");
            }

            if (bidPrice <= product.currentPrice) {
                conn.rollback();
                return BidResult.failure("Giá đặt phải cao hơn giá hiện tại");
            }

            LocalDateTime newEndTime = extendIfNeeded(product.endTime, now);

            bidDAO.updateCurrentPrice(conn, productId, bidPrice);

            if (newEndTime != null) {
                bidDAO.updateEndTime(conn, productId, newEndTime);
            } else {
                newEndTime = product.endTime;
            }

            bidDAO.insertBid(conn, productId, normalizedBidderUsername, bidPrice);

            conn.commit();

            return BidResult.success("Đặt giá thành công", bidPrice, newEndTime, product.name);

        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return BidResult.failure("Đặt giá thất bại");
        } finally {
            closeQuietly(conn);
        }
    }

    private ProductSnapshot lockProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT name, current_price, status, start_time, end_time, seller_username FROM products WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                LocalDateTime startTime = rs.getObject("start_time", LocalDateTime.class);
                LocalDateTime endTime = rs.getObject("end_time", LocalDateTime.class);

                return new ProductSnapshot(
                        rs.getString("name"),
                        rs.getDouble("current_price"),
                        rs.getString("status"),
                        startTime,
                        endTime,
                        rs.getString("seller_username")
                );
            }
        }
    }

    private boolean isAuctionFinished(String status, LocalDateTime endTime, LocalDateTime now) {
        return ProductStatus.isFinished(status, endTime, now);
    }

    private boolean isAuctionNotStarted(LocalDateTime startTime, LocalDateTime now) {
        return startTime != null && startTime.isAfter(now);
    }

    private LocalDateTime extendIfNeeded(LocalDateTime endTime, LocalDateTime now) {
        if (endTime == null) {
            return null;
        }

        long secondsLeft = Duration.between(now, endTime).getSeconds();

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
        String name;
        double currentPrice;
        String status;
        LocalDateTime startTime;
        LocalDateTime endTime;
        String sellerUsername;

        ProductSnapshot(String name, double currentPrice, String status, LocalDateTime startTime, LocalDateTime endTime, String sellerUsername) {
            this.name = name;
            this.currentPrice = currentPrice;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.sellerUsername = sellerUsername;
        }
    }
}
