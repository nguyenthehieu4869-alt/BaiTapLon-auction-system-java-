package com.auction.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;

public final class ProductImageData {
    public static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    private static final String DATA_PREFIX = "data:image/";
    private static final String BASE64_MARKER = ";base64,";

    private ProductImageData() {
    }

    public static String fromFile(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.isFile()) {
            throw new IllegalArgumentException("Không tìm thấy file ảnh đã chọn.");
        }

        long fileSize = Files.size(imageFile.toPath());
        if (fileSize > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Ảnh sản phẩm không được vượt quá 2 MB.");
        }

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        if (imageBytes.length == 0) {
            throw new IllegalArgumentException("File ảnh đã chọn đang trống.");
        }

        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Ảnh sản phẩm không được vượt quá 2 MB.");
        }

        String mimeType = detectMimeType(imageFile);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("File đã chọn không phải định dạng ảnh được hỗ trợ.");
        }

        return "data:" + mimeType + BASE64_MARKER + Base64.getEncoder().encodeToString(imageBytes);
    }

    public static boolean isEmbeddedImage(String imageReference) {
        if (imageReference == null) {
            return false;
        }

        String normalizedReference = imageReference.toLowerCase(Locale.ROOT);
        return normalizedReference.startsWith(DATA_PREFIX)
                && normalizedReference.indexOf(BASE64_MARKER) > DATA_PREFIX.length();
    }

    public static byte[] decode(String imageReference) {
        if (!isEmbeddedImage(imageReference)) {
            throw new IllegalArgumentException("Dữ liệu ảnh sản phẩm không hợp lệ.");
        }

        int base64Start = imageReference.toLowerCase(Locale.ROOT).indexOf(BASE64_MARKER) + BASE64_MARKER.length();

        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageReference.substring(base64Start));
            if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
                throw new IllegalArgumentException("Dữ liệu ảnh sản phẩm vượt quá giới hạn cho phép.");
            }

            return imageBytes;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dữ liệu ảnh sản phẩm không hợp lệ hoặc vượt quá 2 MB.", e);
        }
    }

    private static String detectMimeType(File imageFile) throws IOException {
        String mimeType = Files.probeContentType(imageFile.toPath());
        if (mimeType != null && mimeType.startsWith("image/")) {
            return mimeType;
        }

        String fileName = imageFile.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".png")) {
            return "image/png";
        }

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }

        if (fileName.endsWith(".bmp")) {
            return "image/bmp";
        }

        if (fileName.endsWith(".webp")) {
            return "image/webp";
        }

        return null;
    }
}
