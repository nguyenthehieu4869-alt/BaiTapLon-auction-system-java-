package com.auction.service;

import com.auction.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();

        String sql = "SELECT id,name,description,start_price,current_price,status FROM products";

        try {
            Connection conn = DBConnection.getConnection();

            if (conn == null){
                System.out.println("Không có kết nối DB nên không load được sản phẩm");
                return products;
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("start_price"),
                        rs.getDouble("current_price"),
                        rs.getString("status")
                );

                products.add(product);
            }

            rs.close();
            stmt.close();
            conn.close();


        } catch (Exception e) {
            System.out.println("Lỗi khi load product");
            e.printStackTrace();
        }

        return products;
    }

    public boolean updateCurrentPrice(int productId,double newPrice){
        String sql= "UPDATE products SET current_price = ? WHERE id = ?";

        try {
            Connection conn=DBConnection.getConnection();

            if (conn == null){
                return false;
            }

            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setDouble(1,newPrice);
            stmt.setInt(2,productId);

            int rows=stmt.executeUpdate();

            stmt.close();
            conn.close();

            return rows>0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addProduct(String name, String description, double startPrice) {
        String sql = "INSERT INTO products (name, description, start_price, current_price, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, startPrice);
            stmt.setDouble(4, startPrice);
            stmt.setString(5, "OPEN");

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn =DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(int id, String name, String description, double startPrice, String status) {
        String sql = "UPDATE products SET name = ?, description = ?, start_price = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, startPrice);
            stmt.setString(4, status);
            stmt.setInt(5, id);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeAuction(int productId) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "CLOSED");
            stmt.setInt(2, productId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}