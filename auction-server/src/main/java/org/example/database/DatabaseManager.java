package org.example.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseManager {
    private static final Properties CONFIG = loadConfig();

    private static final String URL = getConfig("db.url", "AUCTION_DB_URL", "jdbc:mysql://localhost:3306/auction_db");
    private static final String USER = getConfig("db.user", "AUCTION_DB_USER", "root");
    private static final String PASS = getConfig("db.password", "AUCTION_DB_PASSWORD", "");

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
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
