package org.example.database;

import org.example.common.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public UserRole login(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? UserRole.fromDatabaseValue(rs.getString("role")) : null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean register(String username, String email, String password, UserRole role) {
        String sql = "INSERT INTO users(username, email, password, role, wallet_balance) VALUES (?, ?, ?, ?, 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role.name());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getEmailByUsername(String username) {
        String sql = "SELECT email FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public double getWalletBalance(String username) {
        String sql = "SELECT wallet_balance FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("wallet_balance");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0D;
    }

    public boolean addWalletBalance(Connection conn, String username, double amount) throws Exception {
        String sql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deductWalletBalance(Connection conn, String username, double amount) throws Exception {
        String sql = "UPDATE users SET wallet_balance = wallet_balance - ? WHERE username = ? AND wallet_balance >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, username);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        }
    }
}
