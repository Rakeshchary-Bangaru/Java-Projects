
package com.ecommerce.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "app.images.storage.type",
        havingValue = "s3"
)
public class S3ProductImageStorageService
        implements ProductImageStorageService {

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private static final long MAX_PIXELS =
            20_000_000L;

    private static final String STORAGE_KEY_PATTERN =
            "[0-9a-fA-F-]{36}\\.png";

    private final S3Client s3Client;
    private final String bucket;
    private final String prefix;

    public S3ProductImageStorageService(
            S3Client s3Client,
            @Value("${app.images.s3.bucket}") String bucket,
            @Value("${app.images.s3.prefix:product-images/}")
            String prefix
    ) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException(
                    "S3 product-image bucket is required"
            );
        }

        if (prefix == null || prefix.isBlank()) {
            this.prefix = "";
        } else {
            if (prefix.startsWith("/")
                    || prefix.contains("..")
                    || prefix.contains("\\")) {
                throw new IllegalArgumentException(
                        "Invalid S3 image prefix"
                );
            }

            this.prefix = prefix.endsWith("/")
                    ? prefix
                    : prefix + "/";
        }

        this.bucket = bucket;
        this.s3Client = s3Client;
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

                if (!format.equals("png")
                        && !format.equals("jpeg")
                        && !format.equals("jpg")) {
                    throw new IllegalArgumentException(
                            "Only PNG and JPEG images are supported"
                    );
                }

                reader.setInput(imageStream, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || (long) width * height > MAX_PIXELS) {
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

        byte[] bytes;

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            if (!ImageIO.write(image, "png", output)) {
                throw new IOException(
                        "PNG encoder unavailable"
                );
            }

            bytes = output.toByteArray();
        }

        String storageKey =
                UUID.randomUUID() + ".png";

        String objectKey = resolveObjectKey(storageKey);

        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType("image/png")
                            .contentLength((long) bytes.length)
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );

            return new StoredProductImage(
                    storageKey,
                    "image/png",
                    bytes.length
            );

        } catch (RuntimeException ex) {
            throw new IOException(
                    "Failed to store product image in S3",
                    ex
            );
        }
    }

    @Override
    public Resource load(String storageKey)
            throws IOException {

        String objectKey = resolveObjectKey(storageKey);

        try {
            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build();

            byte[] bytes = s3Client
                    .getObjectAsBytes(request)
                    .asByteArray();

            return new ByteArrayResource(bytes);

        } catch (RuntimeException ex) {
            throw new IOException(
                    "Product image could not be loaded",
                    ex
            );
        }
    }

    @Override
    public void delete(String storageKey)
            throws IOException {

        String objectKey = resolveObjectKey(storageKey);

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build();

            s3Client.deleteObject(request);

        } catch (RuntimeException ex) {
            throw new IOException(
                    "Product image could not be deleted",
                    ex
            );
        }
    }

    private String resolveObjectKey(String storageKey) {

        if (storageKey == null
                || !storageKey.matches(
                STORAGE_KEY_PATTERN
        )) {
            throw new IllegalArgumentException(
                    "Invalid image storage key"
            );
        }

        return prefix + storageKey;
    }
}
