package com.auction.service;

import com.auction.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ProductDAO {
    private static final String IMAGE_PATH_COLUMN = "image_path";

    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Khong co ket noi DB nen khong load duoc san pham");
                return products;
            }

            boolean hasImagePathColumn = ensureImagePathColumn(conn);
            String sql = buildProductSelectSql(
                    hasImagePathColumn,
                    "WHERE status <> 'DELETED'"
            );

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    products.add(mapProduct(rs, hasImagePathColumn));
                }
            }

        } catch (Exception e) {
            System.out.println("Loi khi load product");
            e.printStackTrace();
        }

        return products;
    }

    public ObservableList<Product> getProductsBySeller(String sellerUsername) {
        ObservableList<Product> products = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Khong co ket noi DB nen khong load duoc san pham cua seller");
                return products;
            }

            boolean hasImagePathColumn = ensureImagePathColumn(conn);
            String sql = buildProductSelectSql(
                    hasImagePathColumn,
                    "WHERE status <> 'DELETED' AND seller_username = ?"
            );

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, sellerUsername);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(mapProduct(rs, hasImagePathColumn));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Loi khi load product theo seller");
            e.printStackTrace();
        }

        return products;
    }

    public boolean updateCurrentPrice(int productId, double newPrice) {
        String sql = "UPDATE products SET current_price = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, newPrice);
                stmt.setInt(2, productId);

                int rows = stmt.executeUpdate();
                return rows > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addProduct(String name, String description, double startPrice,
                              LocalDateTime startTime, LocalDateTime endTime,
                              String sellerUsername, String imagePath) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            boolean hasImagePathColumn = ensureImagePathColumn(conn);
            String normalizedImagePath = normalizeImagePath(imagePath);

            if (normalizedImagePath != null && !hasImagePathColumn) {
                System.out.println("Khong the luu anh san pham vi DB chua co cot image_path");
                return false;
            }

            String sql = hasImagePathColumn
                    ? """
                    INSERT INTO products
                    (name, description, image_path, start_price, current_price, status, start_time, end_time, seller_username)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                    : """
                    INSERT INTO products
                    (name, description, start_price, current_price, status, start_time, end_time, seller_username)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, description);

                if (hasImagePathColumn) {
                    stmt.setString(3, normalizedImagePath);
                    stmt.setDouble(4, startPrice);
                    stmt.setDouble(5, startPrice);
                    stmt.setString(6, "OPEN");
                    stmt.setTimestamp(7, Timestamp.valueOf(startTime));
                    stmt.setTimestamp(8, Timestamp.valueOf(endTime));
                    stmt.setString(9, sellerUsername);
                } else {
                    stmt.setDouble(3, startPrice);
                    stmt.setDouble(4, startPrice);
                    stmt.setString(5, "OPEN");
                    stmt.setTimestamp(6, Timestamp.valueOf(startTime));
                    stmt.setTimestamp(7, Timestamp.valueOf(endTime));
                    stmt.setString(8, sellerUsername);
                }

                int rows = stmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        String sql = "UPDATE products SET status = 'DELETED' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productId);

                int rows = stmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(int id, String name, String description,
                                 double startPrice, String status, String imagePath) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            boolean hasImagePathColumn = ensureImagePathColumn(conn);
            String normalizedImagePath = normalizeImagePath(imagePath);

            if (normalizedImagePath != null && !hasImagePathColumn) {
                System.out.println("Khong the luu anh san pham vi DB chua co cot image_path");
                return false;
            }

            String sql = hasImagePathColumn
                    ? "UPDATE products SET name = ?, description = ?, start_price = ?, status = ?, image_path = ? WHERE id = ?"
                    : "UPDATE products SET name = ?, description = ?, start_price = ?, status = ? WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setDouble(3, startPrice);
                stmt.setString(4, status);

                if (hasImagePathColumn) {
                    stmt.setString(5, normalizedImagePath);
                    stmt.setInt(6, id);
                } else {
                    stmt.setInt(5, id);
                }

                int rows = stmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeAuction(int productId) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "CLOSED");
                stmt.setInt(2, productId);

                int rows = stmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildProductSelectSql(boolean hasImagePathColumn, String whereClause) {
        String selectColumns = hasImagePathColumn
                ? "id, name, description, start_price, current_price, status, start_time, end_time, image_path"
                : "id, name, description, start_price, current_price, status, start_time, end_time";

        return "SELECT " + selectColumns + " FROM products " + whereClause;
    }

    private Product mapProduct(ResultSet rs, boolean hasImagePathColumn) throws SQLException {
        Product product = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("start_price"),
                rs.getDouble("current_price"),
                rs.getString("status")
        );

        Timestamp startTimestamp = rs.getTimestamp("start_time");
        Timestamp endTimestamp = rs.getTimestamp("end_time");

        if (startTimestamp != null) {
            product.setStartTime(startTimestamp.toLocalDateTime());
        }

        if (endTimestamp != null) {
            product.setEndTime(endTimestamp.toLocalDateTime());
        }

        if (hasImagePathColumn) {
            product.setImagePath(normalizeImagePath(rs.getString(IMAGE_PATH_COLUMN)));
        }

        return product;
    }

    private boolean ensureImagePathColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "products", IMAGE_PATH_COLUMN)) {
            return true;
        }

        String sql = "ALTER TABLE products ADD COLUMN image_path VARCHAR(500) NULL";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Khong the tao cot image_path. Tinh nang anh se bi bo qua.");
            return false;
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }

        try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null,
                tableName.toUpperCase(), columnName.toUpperCase())) {
            return columns.next();
        }
    }

    private String normalizeImagePath(String imagePath) {
        if (imagePath == null) {
            return null;
        }

        String trimmed = imagePath.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
