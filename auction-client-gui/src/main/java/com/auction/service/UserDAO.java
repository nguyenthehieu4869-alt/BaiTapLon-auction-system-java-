package com.auction.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public boolean checkLogin(String username,String password){
        String sql="SELECT 1 FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String username,String email, String password) {
        String sql = "INSERT INTO users(username,email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3,password);

                stmt.executeUpdate();

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
