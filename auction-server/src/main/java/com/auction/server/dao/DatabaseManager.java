package com.auction.server.dao;

import com.auction.common.AuctionTime;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;

public class DatabaseManager {
    private static final Properties CONFIG = loadConfig();

    private static final String URL = getConfig("db.url", "AUCTION_DB_URL", "jdbc:mysql://localhost:3306/auction_db");
    private static final String USER = getConfig("db.user", "AUCTION_DB_USER", "root");
    private static final String PASS = getConfig("db.password", "AUCTION_DB_PASSWORD", "");

    public static Connection getConnection() throws Exception {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        configureSessionTimeZone(conn);
        return conn;
    }

    public static void ensureSchema() throws Exception {
        try (Connection conn = getConnection()) {
            ensureTableExists(conn, "users");
            ensureTableExists(conn, "products");
            ensureColumn(conn, "users", "wallet_balance", "ALTER TABLE users ADD COLUMN wallet_balance DOUBLE NOT NULL DEFAULT 0");
            ensureColumn(conn, "products", "image_path", "ALTER TABLE products ADD COLUMN image_path LONGTEXT NULL");
            ensureColumn(conn, "products", "current_price", "ALTER TABLE products ADD COLUMN current_price DOUBLE NOT NULL DEFAULT 0");
            ensureColumn(conn, "products", "start_time", "ALTER TABLE products ADD COLUMN start_time DATETIME NULL");
            ensureColumn(conn, "products", "end_time", "ALTER TABLE products ADD COLUMN end_time DATETIME NULL");
            ensureColumn(conn, "products", "seller_username", "ALTER TABLE products ADD COLUMN seller_username VARCHAR(50) NULL");
            ensureColumn(conn, "products", "status", "ALTER TABLE products ADD COLUMN status ENUM('OPENING', 'FINISHED', 'COMING SOON') NOT NULL DEFAULT 'OPENING'");

            if (!"longtext".equalsIgnoreCase(getColumnDataType(conn, "products", "image_path"))) {
                execute(conn, "ALTER TABLE products MODIFY COLUMN image_path LONGTEXT NULL");
            }

            execute(conn, "UPDATE products SET current_price = start_price WHERE current_price = 0 OR current_price IS NULL");
            execute(conn, """
                    UPDATE products
                    SET status = CASE
                        WHEN UPPER(REPLACE(REPLACE(status, '_', ''), ' ', '')) = 'COMINGSOON' THEN 'COMING SOON'
                        WHEN UPPER(status) IN ('FINISHED', 'CLOSED', 'CLOSE', 'ENDED', 'DONE', 'DELETED') THEN 'FINISHED'
                        ELSE 'OPENING'
                    END
                    WHERE status IS NULL
                       OR status NOT IN ('OPENING', 'FINISHED', 'COMING SOON')
                    """);
            execute(conn, "ALTER TABLE products MODIFY COLUMN status ENUM('OPENING', 'FINISHED', 'COMING SOON') NOT NULL DEFAULT 'OPENING'");
        }
    }

    private static void ensureTableExists(Connection conn, String tableName) throws Exception {
        String sql = """
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Thieu bang " + tableName + " trong database. Hay import database/auction_db.sql.");
                }
            }
        }
    }

    private static void ensureColumn(Connection conn, String tableName, String columnName, String addColumnSql) throws Exception {
        if (getColumnDataType(conn, tableName, columnName) == null) {
            execute(conn, addColumnSql);
        }
    }

    private static String getColumnDataType(Connection conn, String tableName, String columnName) throws Exception {
        String sql = """
                SELECT DATA_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("DATA_TYPE") : null;
            }
        }
    }

    private static void execute(Connection conn, String sql) throws Exception {
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void configureSessionTimeZone(Connection conn) throws Exception {
        ZoneOffset offset = AuctionTime.zone().getRules().getOffset(Instant.now());
        String mysqlOffset = offset.getId().equals("Z") ? "+00:00" : offset.getId();

        try (Statement statement = conn.createStatement()) {
            statement.execute("SET time_zone = '" + mysqlOffset + "'");
        }
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();

        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private static String getConfig(String propertyKey, String envKey, String defaultValue) {
        String systemValue = System.getProperty(propertyKey);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return CONFIG.getProperty(propertyKey, defaultValue);
    }
}
