package com.auction.util;

import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FxmlUtil {
    private FxmlUtil() {
    }

    public static FXMLLoader createLoader(Class<?> contextClass, String resourcePath) {
        URL resource = contextClass.getResource(resourcePath);

        if (resource == null) {
            resource = findSourceResource(resourcePath);
        }

        if (resource == null) {
            throw new IllegalStateException("Khong tim thay FXML: " + resourcePath);
        }

        return new FXMLLoader(resource);
    }

    private static URL findSourceResource(String resourcePath) {
        String relativePath = resourcePath.startsWith("/")
                ? resourcePath.substring(1)
                : resourcePath;

        Path sourcePath = Path.of("src", "main", "resources", relativePath);

        if (!Files.exists(sourcePath)) {
            return null;
        }

        try {
            return sourcePath.toUri().toURL();
        } catch (Exception e) {
            return null;
        }
    }
}
