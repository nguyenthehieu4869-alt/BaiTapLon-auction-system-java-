package com.auction.util;

import javafx.scene.image.Image;
import org.example.common.ProductImageData;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public final class ProductImageUtil {
    private ProductImageUtil() {
    }

    public static String toPortableReference(String imageReference, boolean preserveMissingLegacyPath) throws IOException {
        if (imageReference == null || imageReference.isBlank() || ProductImageData.isEmbeddedImage(imageReference)) {
            return imageReference;
        }

        File imageFile = new File(imageReference);
        if (!imageFile.isFile() && preserveMissingLegacyPath) {
            return imageReference;
        }

        return ProductImageData.fromFile(imageFile);
    }

    public static Image loadImage(String imageReference, double width, double height) {
        if (imageReference == null || imageReference.isBlank()) {
            throw new IllegalArgumentException("Chưa có ảnh sản phẩm.");
        }

        if (ProductImageData.isEmbeddedImage(imageReference)) {
            return new Image(
                    new ByteArrayInputStream(ProductImageData.decode(imageReference)),
                    width,
                    height,
                    true,
                    true
            );
        }

        File imageFile = new File(imageReference);
        if (!imageFile.isFile()) {
            throw new IllegalArgumentException("Không tìm thấy file ảnh.");
        }

        return new Image(imageFile.toURI().toString(), width, height, true, true);
    }

    public static boolean isEmbeddedImage(String imageReference) {
        return ProductImageData.isEmbeddedImage(imageReference);
    }

    public static String getDisplayName(String imageReference) {
        if (ProductImageData.isEmbeddedImage(imageReference)) {
            return "ảnh đã tải lên";
        }

        return imageReference == null || imageReference.isBlank()
                ? ""
                : new File(imageReference).getName();
    }
}
