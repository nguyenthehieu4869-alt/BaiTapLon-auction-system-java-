package com.auction.server.service;

import com.auction.common.AuctionTime;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.DatabaseManager;
import com.auction.server.dao.UserDAO;
import com.auction.server.domain.exception.AuctionClosedException;
import com.auction.server.domain.exception.InvalidBidException;
import com.auction.server.domain.model.Auction;
import com.auction.server.domain.model.BidTransaction;
import com.auction.server.domain.model.Bidder;

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
    private final DomainAuctionMapper auctionMapper = new DomainAuctionMapper();
    private final UserDAO userDAO = new UserDAO();

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

            Auction auction = auctionMapper.toDomainAuction(product);
            auction.addObserver(new AuctionBidObserver());

            BidTransaction transaction;
            try {
                transaction = auction.placeBid(new Bidder(normalizedBidderUsername), bidPrice);
            } catch (InvalidBidException | AuctionClosedException e) {
                conn.rollback();
                return BidResult.failure(e.getMessage());
            }

            double previousHighestBid = bidDAO.getHighestBidByBidderForProduct(conn, productId, normalizedBidderUsername);
            double additionalDebit = transaction.getBidPrice() - previousHighestBid;

            if (additionalDebit > 0 && !deductWallet(conn, normalizedBidderUsername, additionalDebit)) {
                conn.rollback();
                return BidResult.failure("Số dư ví không đủ,vui lòng nạp thêm !");
            }

            LocalDateTime now = AuctionTime.now();

            LocalDateTime newEndTime = extendIfNeeded(product.endTime, now);

            bidDAO.updateCurrentPrice(conn, productId, auction.getCurrentPrice());

            if (newEndTime != null) {
                bidDAO.updateEndTime(conn, productId, newEndTime);
            } else {
                newEndTime = product.endTime;
            }

            bidDAO.insertBid(conn, productId, normalizedBidderUsername, transaction.getBidPrice());

            conn.commit();

            return BidResult.success("Đặt giá thành công", auction.getCurrentPrice(), newEndTime, product.name);

        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return BidResult.failure("Đặt giá thất bại");
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean addWalletBalance(String username, double amount) {
        if (username == null || username.isBlank() || !Double.isFinite(amount) || amount <= 0) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            boolean updated = userDAO.addWalletBalance(conn, username.trim(), amount);
            if (!updated) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    private boolean deductWallet(Connection conn, String username, double amount) throws Exception {
        return userDAO.deductWalletBalance(conn, username, amount);
    }

    private ProductSnapshot lockProduct(Connection conn, int productId) throws SQLException {
        String sql = "SELECT name, start_price, current_price, status, start_time, end_time, seller_username FROM products WHERE id = ? FOR UPDATE";

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
                        rs.getDouble("start_price"),
                        rs.getDouble("current_price"),
                        rs.getString("status"),
                        startTime,
                        endTime,
                        rs.getString("seller_username")
                );
            }
        }
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

    static final class ProductSnapshot {
        final String name;
        final double startPrice;
        final double currentPrice;
        final String status;
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final String sellerUsername;

        ProductSnapshot(String name, double startPrice, double currentPrice, String status, LocalDateTime startTime, LocalDateTime endTime, String sellerUsername) {
            this.name = name;
            this.startPrice = startPrice;
            this.currentPrice = currentPrice;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.sellerUsername = sellerUsername;
        }
    }
}
