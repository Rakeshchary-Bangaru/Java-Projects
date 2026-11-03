
package com.ecommerce.storage;

import com.ecommerce.config.ProductImageStorageProperties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalProductImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStoreLoadAndDeleteImage() throws Exception {

        var storage = new LocalProductImageStorageService(
                new ProductImageStorageProperties(
                        tempDir.toString()
                )
        );

        BufferedImage image =
                new BufferedImage(
                        20,
                        20,
                        BufferedImage.TYPE_INT_RGB
                );

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ImageIO.write(image, "png", output);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                output.toByteArray()
        );

        var stored = storage.store(file);

        assertNotNull(stored.storageKey());

        assertEquals(
                "image/png",
                stored.contentType()
        );

        assertTrue(stored.fileSize() > 0);

        var resource = storage.load(
                stored.storageKey()
        );

        assertTrue(resource.exists());

        storage.delete(stored.storageKey());

        assertFalse(resource.exists());
    }

    @Test
    void shouldRejectInvalidImage() throws Exception {

        var storage = new LocalProductImageStorageService(
                new ProductImageStorageProperties(
                        tempDir.toString()
                )
        );

        MockMultipartFile fakeImage = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not a real image".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storage.store(fakeImage)
        );
    }

    @Test
    void shouldRejectPathTraversal() throws Exception {

        var storage = new LocalProductImageStorageService(
                new ProductImageStorageProperties(
                        tempDir.toString()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storage.load("../../secret.txt")
        );
    }
}
