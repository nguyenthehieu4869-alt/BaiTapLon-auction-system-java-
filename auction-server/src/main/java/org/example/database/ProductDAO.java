package org.example.database;

import org.example.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

   public List<Product> getAllProducts() {
      List<Product> products = new ArrayList<>();

      String sql = """
                SELECT id, name, description, image_path, start_price, current_price,
                       status, start_time, end_time, seller_username
                FROM products
                WHERE status <> 'DELETED'
                """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery()) {

         while (rs.next()) {
            products.add(mapProduct(rs));
         }

      } catch (Exception e) {
         e.printStackTrace();
      }

      return products;
   }

   private Product mapProduct(ResultSet rs) throws SQLException {
      Timestamp start = rs.getTimestamp("start_time");
      Timestamp end = rs.getTimestamp("end_time");

      return new Product(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("description"),
              rs.getString("image_path"),
              rs.getDouble("start_price"),
              rs.getDouble("current_price"),
              rs.getString("status"),
              start == null ? null : start.toLocalDateTime().toString(),
              end == null ? null : end.toLocalDateTime().toString(),
              rs.getString("seller_username")
      );
   }

   public List<Product> getProductsBySeller(String sellerUsername) {
      List<Product> products = new ArrayList<>();

      String sql = """
            SELECT id, name, description, image_path, start_price, current_price,
                   status, start_time, end_time, seller_username
            FROM products
            WHERE status <> 'DELETED' AND seller_username = ?
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
         ps.setString(6, status);
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
         ps.setString(4, status);
         ps.setString(5, imagePath);
         ps.setInt(6, id);

         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean deleteProduct(int productId) {
      String sql = "UPDATE products SET status = 'DELETED' WHERE id = ?";

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, productId);
         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public boolean closeAuction(int productId) {
      String sql = "UPDATE products SET status = 'CLOSED' WHERE id = ?";

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, productId);
         return ps.executeUpdate() > 0;

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }
}