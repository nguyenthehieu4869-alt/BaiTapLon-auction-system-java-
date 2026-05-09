package com.auction.service;

import com.auction.model.Product;

public class BidService {
    private final ProductDAO productDAO;
    private final BidDAO bidDAO;

    public BidService() {
        this.productDAO = new ProductDAO();
        this.bidDAO = new BidDAO();
    }

    public BidResult placeBid(Product product, String username, double newPrice) {
        if (product == null) {
            return BidResult.failure("Không tìm thấy sản phẩm");
        }

        if ("CLOSED".equalsIgnoreCase(product.getStatus())) {
            return BidResult.failure("Sản phẩm đã đóng đấu giá");
        }

        if (newPrice <= product.getCurrentPrice()) {
            return BidResult.failure("Giá đặt phải cao hơn giá hiện tại");
        }

        String bidder = username != null && !username.trim().isEmpty()
                ? username
                : "Guest";

        boolean updated = productDAO.updateCurrentPrice(product.getId(), newPrice);

        if (!updated) {
            return BidResult.failure("Cập nhật giá sản phẩm thất bại");
        }

        boolean bidSaved = bidDAO.addBid(product.getId(), bidder, newPrice);

        if (!bidSaved) {
            return BidResult.failure("Đặt giá thành công nhưng chưa lưu được lịch sử");
        }

        return BidResult.success("Đặt giá thành công");
    }
}