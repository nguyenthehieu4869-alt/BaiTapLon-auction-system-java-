package org.example.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductImageDataTest {
    @TempDir
    Path tempDir;

    @Test
    void encodesAndDecodesImageFile() throws Exception {
        byte[] imageBytes = {1, 2, 3, 4, 5};
        Path imageFile = tempDir.resolve("product.png");
        Files.write(imageFile, imageBytes);

        String imageReference = ProductImageData.fromFile(imageFile.toFile());

        assertTrue(ProductImageData.isEmbeddedImage(imageReference));
        assertArrayEquals(imageBytes, ProductImageData.decode(imageReference));
    }

    @Test
    void rejectsFileAboveSizeLimit() throws Exception {
        Path imageFile = tempDir.resolve("large.jpg");
        Files.write(imageFile, new byte[ProductImageData.MAX_IMAGE_BYTES + 1]);

        assertThrows(IllegalArgumentException.class, () -> ProductImageData.fromFile(imageFile.toFile()));
    }

    @Test
    void doesNotTreatLocalPathAsEmbeddedImage() {
        assertFalse(ProductImageData.isEmbeddedImage("C:\\images\\product.png"));
    }
}
