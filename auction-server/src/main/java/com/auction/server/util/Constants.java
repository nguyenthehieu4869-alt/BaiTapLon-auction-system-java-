package com.auction.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
    private static final Properties CONFIG = loadConfig();

    public static final String HOST = getConfig("server.host", "AUCTION_SERVER_HOST", "localhost");
    public static final int PORT = getIntConfig("server.port", "AUCTION_SERVER_PORT", 9999);

    private static Properties loadConfig() {
        Properties properties = new Properties();

        try (InputStream input = Constants.class.getClassLoader().getResourceAsStream("config.properties")) {
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

    private static int getIntConfig(String propertyKey, String envKey, int defaultValue) {
        String value = getConfig(propertyKey, envKey, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
