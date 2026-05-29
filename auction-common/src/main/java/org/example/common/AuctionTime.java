package org.example.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

public final class AuctionTime {
    private static final String DEFAULT_ZONE_ID = "Asia/Ho_Chi_Minh";
    private static final String TIMEZONE_PROPERTY = "auction.timezone";
    private static final String TIMEZONE_ENV = "AUCTION_TIME_ZONE";
    private static final ZoneId AUCTION_ZONE = resolveZone();

    private AuctionTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(AUCTION_ZONE);
    }

    public static ZoneId zone() {
        return AUCTION_ZONE;
    }

    public static void installAsDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(AUCTION_ZONE));
    }

    private static ZoneId resolveZone() {
        String configuredZone = System.getProperty(TIMEZONE_PROPERTY);

        if (configuredZone == null || configuredZone.isBlank()) {
            configuredZone = System.getenv(TIMEZONE_ENV);
        }

        if (configuredZone != null && !configuredZone.isBlank()) {
            try {
                return ZoneId.of(configuredZone.trim());
            } catch (Exception ignored) {
                // Fall through to the app default when deployment config is invalid.
            }
        }

        return ZoneId.of(DEFAULT_ZONE_ID);
    }
}
