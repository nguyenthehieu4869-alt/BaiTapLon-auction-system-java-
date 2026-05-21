package org.example.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

   public List<String> getAllProducts() {

      List<String> list = new ArrayList<>();

      try {
         Connection conn = DatabaseManager.getConnection();

         String sql = "SELECT * FROM products";
         PreparedStatement ps = conn.prepareStatement(sql);

         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            double price = rs.getDouble("current_price");

            String product = id + " - " + name + " - Giá: " + price;
            list.add(product);
         }

      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }
}
