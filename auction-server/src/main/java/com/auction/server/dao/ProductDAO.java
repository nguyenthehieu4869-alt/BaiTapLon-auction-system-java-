package com.auction.server.dao;

import com.auction.common.AuctionTime;
import com.auction.common.ProductStatus;
import com.auction.server.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

   public List<Product> getAllProducts() {
      List<Product> products = new ArrayList<>();

      String sql = """
                SELECT id, name, description, image_path, start_price, current_price,
                       status, start_time, end_time, seller_username
                FROM products
                """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery()) {

         while (rs.next()) {
            products.add(mapProduct(rs));
         }

      } catch (Exception e) {
         e.printStackTrace();
         throw new IllegalStateException("Không tải được danh sách sản phẩm từ DB: " + e.getMessage(), e);
      }

      return products;
   }

   private Product mapProduct(ResultSet rs) throws SQLException {
      LocalDateTime startTime = rs.getObject("start_time", LocalDateTime.class);
      LocalDateTime endTime = rs.getObject("end_time", LocalDateTime.class);

      return new Product(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("description"),
              rs.getString("image_path"),
              rs.getDouble("start_price"),
              rs.getDouble("current_price"),
              ProductStatus.current(startTime, endTime, rs.getString("status")),
              startTime == null ? null : startTime.toString(),
              endTime == null ? null : endTime.toString(),
              rs.getString("seller_username")
      );
   }

   public List<Product> getProductsBySeller(String sellerUsername) {
      List<Product> products = new ArrayList<>();

      String sql = """
            SELECT id, name, description, image_path, start_price, current_price,
                   status, start_time, end_time, seller_username
            FROM products
            WHERE seller_username = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, sellerUsername);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               products.add(mapProduct(rs));
            }
         }

      } catch (Exception e) {
         e.printStackTrace();
         throw new IllegalStateException("Không tải được sản phẩm của seller từ DB: " + e.getMessage(), e);
      }

      return products;
   }

    public boolean addProduct(String name, String description, String imagePath,
                              double startPrice, String status, LocalDateTime startTime,
                              LocalDateTime endTime, String sellerUsername) {
        String sql = """
         INSERT INTO products
         (name, description, image_path, start_price, current_price, status, start_time, end_time, seller_username)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
         """;

        try {
            DatabaseManager.ensureSchema();

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, description);
                ps.setString(3, imagePath);
                ps.setDouble(4, startPrice);
                ps.setDouble(5, startPrice);
                ps.setString(6, ProductStatus.current(startTime, endTime, status));
                ps.setObject(7, startTime);
                ps.setObject(8, endTime);
                ps.setString(9, sellerUsername);

                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Không thêm được sản phẩm vào DB: " + e.getMessage(), e
            );
        }
    }


   public boolean editProductBySeller(int id, String name, String description,
                                      double startPrice, String status, String imagePath,
                                      String sellerUsername) {
      String sql = """
            UPDATE products
            SET name = ?, description = ?, start_price = ?, status = ?, image_path = ?
            WHERE id = ? AND seller_username = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, name);
         ps.setString(2, description);
         ps.setDouble(3, startPrice);
         ps.setString(4, ProductStatus.normalize(status));
         ps.setString(5, imagePath);
         ps.setInt(6, id);
         ps.setString(7, sellerUsername);

         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean deleteProduct(int productId) {
      Connection conn = null;
      String deleteBidsSql = "DELETE FROM bids WHERE product_id = ?";
      String deleteProductSql = "DELETE FROM products WHERE id = ?";

      try {
         conn = DatabaseManager.getConnection();
         conn.setAutoCommit(false);

         try (PreparedStatement ps = conn.prepareStatement(deleteBidsSql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
         }

         int deletedProducts;
         try (PreparedStatement ps = conn.prepareStatement(deleteProductSql)) {
            ps.setInt(1, productId);
            deletedProducts = ps.executeUpdate();
         }

         conn.commit();
         return deletedProducts > 0;

      } catch (Exception e) {
         if (conn != null) {
            try {
               conn.rollback();
            } catch (SQLException ignored) {
            }
         }
         e.printStackTrace();
         return false;
      } finally {
         if (conn != null) {
            try {
               conn.close();
            } catch (SQLException ignored) {
            }
         }
      }
   }

   public boolean closeAuction(int productId) {
      String sql = """
            UPDATE products
            SET status = ?,
                end_time = CASE
                    WHEN end_time IS NULL OR end_time > ? THEN ?
                    ELSE end_time
                END
            WHERE id = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         LocalDateTime now = AuctionTime.now();
         ps.setString(1, ProductStatus.FINISHED);
         ps.setObject(2, now);
         ps.setObject(3, now);
         ps.setInt(4, productId);
         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public int countProductsBySeller(String sellerUsername) {
      String sql = """
            SELECT COUNT(*)
            FROM products
            WHERE seller_username = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, sellerUsername);

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
