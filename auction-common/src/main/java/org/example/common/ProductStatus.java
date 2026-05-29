package org.example.common;

import java.time.LocalDateTime;

public final class ProductStatus {
    public static final String OPENING = "OPENING";
    public static final String FINISHED = "FINISHED";
    public static final String COMING_SOON = "COMING SOON";

    private ProductStatus() {
    }

    public static String current(LocalDateTime startTime, LocalDateTime endTime, String storedStatus) {
        return current(startTime, endTime, storedStatus, AuctionTime.now());
    }

    public static String current(LocalDateTime startTime, LocalDateTime endTime, String storedStatus, LocalDateTime now) {
        String normalizedStatus = normalizeOrNull(storedStatus);

        if (FINISHED.equals(normalizedStatus)) {
            return FINISHED;
        }

        if (startTime != null && startTime.isAfter(now)) {
            return COMING_SOON;
        }

        if (endTime != null && !endTime.isAfter(now)) {
            return FINISHED;
        }

        return OPENING;
    }

    public static boolean isFinished(String status, LocalDateTime endTime) {
        return isFinished(status, endTime, AuctionTime.now());
    }

    public static boolean isFinished(String status, LocalDateTime endTime, LocalDateTime now) {
        return FINISHED.equals(normalizeOrNull(status))
                || (endTime != null && !endTime.isAfter(now));
    }

    public static String normalize(String status) {
        String normalizedStatus = normalizeOrNull(status);

        if (normalizedStatus != null) {
            return normalizedStatus;
        }

        throw new IllegalArgumentException("Status không hợp lệ: " + status);
    }

    private static String normalizeOrNull(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String trimmedStatus = status.trim();
        String compactStatus = trimmedStatus
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");

        if (OPENING.equalsIgnoreCase(trimmedStatus)
                || "OPEN".equalsIgnoreCase(trimmedStatus)
                || "ACTIVE".equalsIgnoreCase(trimmedStatus)) {
            return OPENING;
        }

        if (FINISHED.equalsIgnoreCase(trimmedStatus)
                || "CLOSED".equalsIgnoreCase(trimmedStatus)
                || "CLOSE".equalsIgnoreCase(trimmedStatus)
                || "ENDED".equalsIgnoreCase(trimmedStatus)
                || "DONE".equalsIgnoreCase(trimmedStatus)
                || "DELETED".equalsIgnoreCase(trimmedStatus)) {
            return FINISHED;
        }

        if (COMING_SOON.equalsIgnoreCase(trimmedStatus)
                || "COMINGSOON".equalsIgnoreCase(compactStatus)
                || "UPCOMING".equalsIgnoreCase(trimmedStatus)
                || "PENDING".equalsIgnoreCase(trimmedStatus)) {
            return COMING_SOON;
        }

        return null;
    }
}
