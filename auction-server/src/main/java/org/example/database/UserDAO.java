package org.example.database;

import org.example.model.User;
import org.example.model.UserFactory;
import org.example.model.UserRole;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public boolean login(String username, String password) {
        return findByCredentials(username, password) != null;
    }

    public User findByCredentials(String username, String password) {
        ensureRoleColumn();

        String sql = """
                SELECT id, username, email, password, role
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return UserFactory.create(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean register(String username, String email, String password) {
        return register(username, email, password, UserRole.BIDDER.name());
    }

    public boolean register(String username, String email, String password, String role) {
        ensureRoleColumn();

        String sql = "INSERT INTO users(username, email, password, role) VALUES (?, ?, ?, ?)";
        String normalizedRole = normalizeRegistrationRole(role);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, normalizedRole);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String normalizeRegistrationRole(String role) {
        UserRole normalized = UserRole.from(role);
        if (normalized == UserRole.ADMIN) {
            return UserRole.BIDDER.name();
        }

        return normalized.name();
    }

    private void ensureRoleColumn() {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (hasColumn(conn, "users", "role")) {
                return;
            }

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'BIDDER'");
                statement.executeUpdate("UPDATE users SET role = 'SELLER' WHERE username IN ('seller1', 'seller2')");
                statement.executeUpdate("UPDATE users SET role = 'ADMIN' WHERE username IN ('huy', 'hieu', 'kien')");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
