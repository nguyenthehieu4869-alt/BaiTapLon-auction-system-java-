package org.example.database;

import org.example.common.ProductStatus;
import org.example.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
      Timestamp start = rs.getTimestamp("start_time");
      Timestamp end = rs.getTimestamp("end_time");
      LocalDateTime startTime = start == null ? null : start.toLocalDateTime();
      LocalDateTime endTime = end == null ? null : end.toLocalDateTime();

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
                             double startPrice, String status, Timestamp startTime,
                             Timestamp endTime, String sellerUsername) {
      String sql = """
            INSERT INTO products
            (name, description, image_path, start_price, current_price, status, start_time, end_time, seller_username)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, name);
         ps.setString(2, description);
         ps.setString(3, imagePath);
         ps.setDouble(4, startPrice);
         ps.setDouble(5, startPrice);
         ps.setString(6, ProductStatus.current(
                 startTime == null ? null : startTime.toLocalDateTime(),
                 endTime == null ? null : endTime.toLocalDateTime(),
                 status
         ));
         ps.setTimestamp(7, startTime);
         ps.setTimestamp(8, endTime);
         ps.setString(9, sellerUsername);

         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean editProduct(int id, String name, String description,
                                double startPrice, String status, String imagePath) {
      String sql = """
            UPDATE products
            SET name = ?, description = ?, start_price = ?, status = ?, image_path = ?
            WHERE id = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, name);
         ps.setString(2, description);
         ps.setDouble(3, startPrice);
         ps.setString(4, ProductStatus.normalize(status));
         ps.setString(5, imagePath);
         ps.setInt(6, id);

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
                    WHEN end_time IS NULL OR end_time > NOW() THEN NOW()
                    ELSE end_time
                END
            WHERE id = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setString(1, ProductStatus.FINISHED);
         ps.setInt(2, productId);
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
