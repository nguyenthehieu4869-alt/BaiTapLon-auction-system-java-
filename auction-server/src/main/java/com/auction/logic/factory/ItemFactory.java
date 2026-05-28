package com.auction.logic.factory;

import com.auction.logic.model.Art;
import com.auction.logic.model.Electronics;
import com.auction.logic.model.Item;
import com.auction.logic.model.Vehicle;

public class ItemFactory {

    public static Item create(String type, String name, double price) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại sản phẩm");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm");
        }

        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0");
        }

        switch (type.trim().toLowerCase()) {
            case "electronics":
                return new Electronics(name.trim(), price);
            case "art":
                return new Art(name.trim(), price);
            case "vehicle":
                return new Vehicle(name.trim(), price);
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}
