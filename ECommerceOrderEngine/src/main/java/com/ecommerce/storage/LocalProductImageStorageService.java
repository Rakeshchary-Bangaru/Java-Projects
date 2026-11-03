
package com.ecommerce.storage;

import com.ecommerce.config.ProductImageStorageProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
@EnableConfigurationProperties(ProductImageStorageProperties.class)
public class LocalProductImageStorageService
        implements ProductImageStorageService {

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private static final long MAX_PIXELS =
            20_000_000L;

    private final Path storageRoot;

    public LocalProductImageStorageService(
            ProductImageStorageProperties properties
    ) throws IOException {

        if (properties.storageDirectory() == null ||
                properties.storageDirectory().isBlank()) {

            throw new IllegalArgumentException(
                    "Product image storage directory is required"
            );
        }

        this.storageRoot = Path.of(
                properties.storageDirectory()
        ).toAbsolutePath().normalize();

        Files.createDirectories(storageRoot);

        if (Files.isSymbolicLink(storageRoot)) {
            throw new IOException(
                    "Storage directory must not be a symbolic link"
            );
        }
    }

    @Override
    public StoredProductImage store(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "An image file is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Maximum image size is 5 MB"
            );
        }

        BufferedImage image;

        try (InputStream input = file.getInputStream();
             ImageInputStream imageStream =
                     ImageIO.createImageInputStream(input)) {

            if (imageStream == null) {
                throw new IllegalArgumentException(
                        "Invalid image data"
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageStream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Unsupported image format"
                );
            }

            ImageReader reader = readers.next();

            try {
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                if (!format.equals("png") &&
                        !format.equals("jpeg") &&
                        !format.equals("jpg")) {

                    throw new IllegalArgumentException(
                            "Only PNG and JPEG images are supported"
                    );
                }

                reader.setInput(imageStream, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0 ||
                        (long) width * height > MAX_PIXELS) {

                    throw new IllegalArgumentException(
                            "Image dimensions exceed the allowed limit"
                    );
                }

                image = reader.read(0);

            } finally {
                reader.dispose();
            }
        }

        if (image == null) {
            throw new IllegalArgumentException(
                    "Unable to decode image"
            );
        }

        String storageKey =
                UUID.randomUUID() + ".png";

        Path destination = resolveKey(storageKey);

        try {
            try (var output = Files.newOutputStream(
                    destination,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE)) {

                if (!ImageIO.write(image, "png", output)) {
                    throw new IOException(
                            "PNG encoder unavailable"
                    );
                }
            }

            return new StoredProductImage(
                    storageKey,
                    "image/png",
                    Files.size(destination)
            );

        } catch (IOException | RuntimeException ex) {

            Files.deleteIfExists(destination);
            throw ex;
        }
    }

    @Override
    public Resource load(String storageKey)
            throws IOException {

        Path path = resolveKey(storageKey);

        if (!Files.isRegularFile(path) ||
                Files.isSymbolicLink(path)) {

            throw new IOException(
                    "Product image not found"
            );
        }

        Resource resource = new UrlResource(
                path.toUri()
        );

        if (!resource.isReadable()) {
            throw new IOException(
                    "Product image is not readable"
            );
        }

        return resource;
    }

    @Override
    public void delete(String storageKey)
            throws IOException {

        Path path = resolveKey(storageKey);

        if (Files.isSymbolicLink(path)) {
            throw new IOException(
                    "Symbolic links are not allowed"
            );
        }

        Files.deleteIfExists(path);
    }

    private Path resolveKey(String storageKey) {

        if (storageKey == null ||
                !storageKey.matches(
                        "[0-9a-fA-F-]{36}\\.png"
                )) {

            throw new IllegalArgumentException(
                    "Invalid image storage key"
            );
        }

        Path resolved = storageRoot
                .resolve(storageKey)
                .normalize();

        if (!resolved.getParent().equals(storageRoot)) {
            throw new IllegalArgumentException(
                    "Invalid image path"
            );
        }

        return resolved;
    }
}
