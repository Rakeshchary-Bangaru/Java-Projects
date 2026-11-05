
package com.ecommerce.migration;

import com.ecommerce.model.Product;
import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.storage.ProductImageStorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Profile("!test")
@ConditionalOnProperty(
        name = "app.images.import.enabled",
        havingValue = "true"
)
public class ProductImageMigrationRunner
        implements ApplicationRunner {

    private static final String EXPECTED_HEADER =
            "category_folder,filename,suggested_product_name,relative_path";

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89,
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A
    };

    private final SpringDataProductRepository productRepository;
    private final SpringDataProductImageRepository imageRepository;
    private final ProductImageStorageService storageService;
    private final TransactionTemplate transactions;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.images.import.source-dir}")
    private String sourceDir;

    @Value("${app.images.import.apply:false}")
    private boolean apply;

    public ProductImageMigrationRunner(
            SpringDataProductRepository productRepository,
            SpringDataProductImageRepository imageRepository,
            ProductImageStorageService storageService,
            TransactionTemplate transactions,
            JdbcTemplate jdbcTemplate
    ) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.storageService = storageService;
        this.transactions = transactions;
        this.jdbcTemplate = jdbcTemplate;
    }

    private record ImageEntry(
            String categoryFolder,
            String filename,
            String productName,
            String relativePath,
            Path file,
            Product product
    ) {}

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // ==================================================
        // 1. SAFETY: LOCAL DEVELOPMENT DATABASE ONLY
        // ==================================================

        verifyLocalDatabase();

        // ==================================================
        // 2. LOCATE IMAGE MANIFEST
        // ==================================================

        Path root = Path.of(sourceDir)
                .toAbsolutePath()
                .normalize();

        Path manifest = root.resolve("image-manifest.csv");

        if (!Files.isRegularFile(manifest)) {
            throw new IllegalStateException(
                    "Image manifest not found: " + manifest
            );
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println(" V11 PRODUCT IMAGE MIGRATION");
        System.out.println("====================================");
        System.out.println("Source directory: " + root);
        System.out.println("Apply mode: " + apply);
        System.out.println("====================================");

        // ==================================================
        // 3. LOAD AND VALIDATE THE ENTIRE MANIFEST
        // ==================================================

        List<ImageEntry> entries =
                loadAndValidateManifest(root, manifest);

        System.out.println(
                "Validated image entries: " + entries.size()
        );

        // ==================================================
        // 4. PROCESS IMAGES
        // ==================================================

        int matched = 0;
        int skipped = 0;
        int wouldImport = 0;
        int imported = 0;

        for (ImageEntry entry : entries) {

            Product product = entry.product();
            matched++;

            // Never overwrite previously imported images.
            if (imageRepository.existsByProduct_Id(
                    product.getId()
            )) {
                skipped++;

                System.out.println(
                        "SKIP existing images: "
                                + product.getId()
                                + " | "
                                + product.getName()
                );

                continue;
            }

            // Dry run: absolutely no file or DB changes.
            if (!apply) {
                wouldImport++;

                System.out.println(
                        "DRY RUN: "
                                + product.getId()
                                + " | "
                                + product.getCategory()
                                + " | "
                                + product.getName()
                                + " <- "
                                + entry.relativePath()
                );

                continue;
            }

            // Product image metadata is saved in one
            // database transaction.
            transactions.executeWithoutResult(status -> {

                try {
                    MultipartFile multipart =
                            new DiskImage(entry.file());

                    var stored = storageService.store(multipart);

                    // Clean up stored file if DB rolls back.
                    TransactionSynchronizationManager
                            .registerSynchronization(
                                    new TransactionSynchronization() {
                                        @Override
                                        public void afterCompletion(
                                                int result
                                        ) {
                                            if (result
                                                    == STATUS_ROLLED_BACK) {

                                                try {
                                                    storageService.delete(
                                                            stored.storageKey()
                                                    );
                                                } catch (IOException ex) {
                                                    System.err.println(
                                                            "Rollback cleanup failed: "
                                                                    + ex.getMessage()
                                                    );
                                                }
                                            }
                                        }
                                    }
                            );

                    ProductImage image = new ProductImage(
                            product,
                            stored.storageKey(),
                            entry.filename(),
                            stored.contentType(),
                            stored.fileSize(),
                            0,
                            true
                    );

                    imageRepository.saveAndFlush(image);

                } catch (IOException ex) {
                    throw new IllegalStateException(
                            "Failed importing image: "
                                    + entry.file(),
                            ex
                    );
                }
            });

            imported++;

            System.out.println(
                    "IMPORTED: "
                            + product.getId()
                            + " | "
                            + product.getName()
                            + " <- "
                            + entry.relativePath()
            );
        }

        // ==================================================
        // 5. FINAL SUMMARY
        // ==================================================

        System.out.println();
        System.out.println("====================================");
        System.out.println(" V11 IMAGE MIGRATION SUMMARY");
        System.out.println("====================================");
        System.out.println("Manifest entries: " + entries.size());
        System.out.println("Matched products: " + matched);
        System.out.println("Already had images: " + skipped);
        System.out.println("Would import: " + wouldImport);
        System.out.println("Images imported: " + imported);
        System.out.println("Apply mode: " + apply);
        System.out.println("====================================");
    }

    // ======================================================
    // VERIFY LOCAL DATABASE
    // ======================================================

    private void verifyLocalDatabase() throws Exception {

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        if (!"ecommerce_db".equals(database)) {
            throw new IllegalStateException(
                    "Image migration restricted to ecommerce_db. "
                            + "Connected database: "
                            + database
            );
        }

        if (jdbcTemplate.getDataSource() == null) {
            throw new IllegalStateException(
                    "DataSource is unavailable"
            );
        }

        String jdbcUrl;

        try (var connection =
                     jdbcTemplate.getDataSource()
                             .getConnection()) {

            jdbcUrl = connection.getMetaData().getURL();
        }

        if (jdbcUrl == null) {
            throw new IllegalStateException(
                    "Unable to determine JDBC URL"
            );
        }

        boolean localDatabase =
                jdbcUrl.startsWith(
                        "jdbc:mysql://localhost:"
                )
                        || jdbcUrl.startsWith(
                        "jdbc:mysql://127.0.0.1:"
                );

        if (!localDatabase) {
            throw new IllegalStateException(
                    "Image migration is permitted only "
                            + "against local MySQL."
            );
        }
    }

    // ======================================================
    // LOAD, VALIDATE AND MATCH IMAGE ENTRIES
    // ======================================================

    private List<ImageEntry> loadAndValidateManifest(
            Path root,
            Path manifest
    ) throws Exception {

        List<String> lines = Files.readAllLines(manifest);

        if (lines.isEmpty()) {
            throw new IllegalStateException(
                    "Image manifest is empty"
            );
        }

        String header = lines.get(0)
                .replace("\uFEFF", "")
                .trim();

        if (!EXPECTED_HEADER.equals(header)) {
            throw new IllegalStateException(
                    "Invalid manifest header. Expected: "
                            + EXPECTED_HEADER
            );
        }

        List<Product> products = productRepository.findAll();

        List<ImageEntry> entries = new ArrayList<>();
        Set<Long> seenProductIds = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split(",", -1);

            if (columns.length != 4) {
                throw new IllegalStateException(
                        "Invalid manifest row at line "
                                + (i + 1)
                );
            }

            String categoryFolder = columns[0].trim();
            String filename = columns[1].trim();
            String productName = columns[2].trim();
            String relativePath = columns[3].trim();

            if (categoryFolder.isBlank()
                    || filename.isBlank()
                    || productName.isBlank()
                    || relativePath.isBlank()) {

                throw new IllegalStateException(
                        "Missing manifest value at line "
                                + (i + 1)
                );
            }

            // Validate the image path.
            Path file = root.resolve(relativePath)
                    .normalize();

            if (!file.startsWith(root)
                    || !Files.isRegularFile(file)) {

                throw new IllegalStateException(
                        "Missing or invalid image at line "
                                + (i + 1)
                                + ": "
                                + relativePath
                );
            }

            // Ensure filename matches manifest.
            if (!file.getFileName()
                    .toString()
                    .equals(filename)) {

                throw new IllegalStateException(
                        "Filename mismatch at line "
                                + (i + 1)
                );
            }

            // Ensure the image is in the correct
            // category folder.
            Path expectedParent = root.resolve(
                    categoryFolder
            ).normalize();

            if (!file.getParent().equals(expectedParent)) {
                throw new IllegalStateException(
                        "Category folder mismatch at line "
                                + (i + 1)
                );
            }

            // This importer accepts PNG files.
            if (!filename.toLowerCase(Locale.ROOT)
                    .endsWith(".png")) {

                throw new IllegalStateException(
                        "Only PNG files are supported: "
                                + filename
                );
            }

            validatePng(file);

            // Match product by name AND category.
            List<Product> matches = products.stream()
                    .filter(product ->
                            matchesName(
                                    product.getName(),
                                    productName
                            )
                                    && matchesCategory(
                                    product.getCategory(),
                                    categoryFolder
                            )
                    )
                    .toList();

            if (matches.size() != 1) {
                throw new IllegalStateException(
                        "Expected exactly one matching product "
                                + "at line "
                                + (i + 1)
                                + " for "
                                + productName
                                + " / "
                                + categoryFolder
                                + ". Found: "
                                + matches.size()
                );
            }

            Product product = matches.get(0);

            // One primary image per product for this batch.
            if (!seenProductIds.add(product.getId())) {
                throw new IllegalStateException(
                        "Duplicate product image mapping: "
                                + product.getName()
                );
            }

            entries.add(
                    new ImageEntry(
                            categoryFolder,
                            filename,
                            productName,
                            relativePath,
                            file,
                            product
                    )
            );
        }

        if (entries.isEmpty()) {
            throw new IllegalStateException(
                    "Manifest contains no image entries"
            );
        }

        return entries;
    }

    // ======================================================
    // VALIDATE PNG SIGNATURE
    // ======================================================

    private void validatePng(Path file)
            throws IOException {

        byte[] actualSignature = new byte[8];

        try (InputStream input =
                     Files.newInputStream(file)) {

            int read = input.readNBytes(
                    actualSignature,
                    0,
                    actualSignature.length
            );

            if (read != PNG_SIGNATURE.length) {
                throw new IllegalStateException(
                        "Invalid PNG file: " + file
                );
            }
        }

        if (!Arrays.equals(
                actualSignature,
                PNG_SIGNATURE
        )) {
            throw new IllegalStateException(
                    "File is not a valid PNG: " + file
            );
        }
    }

    // ======================================================
    // MATCH PRODUCT NAME
    // ======================================================

    private boolean matchesName(
            String actualName,
            String suggestedName
    ) {
        return normalize(actualName)
                .equals(normalize(suggestedName));
    }

    // ======================================================
    // MATCH PRODUCT CATEGORY
    // ======================================================

    private boolean matchesCategory(
            String actualCategory,
            String categoryFolder
    ) {
        String normalizedCategory =
                normalize(actualCategory)
                        .replace(" ", "-");

        return normalizedCategory.equals(
                normalize(categoryFolder)
        );
    }

    // ======================================================
    // NORMALIZE TEXT
    // ======================================================

    private String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    // ======================================================
    // DISK IMAGE ADAPTER
    // ======================================================

    private static class DiskImage
            implements MultipartFile {

        private final Path path;

        DiskImage(Path path) {
            this.path = path;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return path.getFileName().toString();
        }

        @Override
        public String getContentType() {
            return "image/png";
        }

        @Override
        public boolean isEmpty() {
            return getSize() == 0;
        }

        @Override
        public long getSize() {
            try {
                return Files.size(path);
            } catch (IOException ex) {
                throw new IllegalStateException(
                        "Unable to read image size: "
                                + path,
                        ex
                );
            }
        }

        @Override
        public byte[] getBytes()
                throws IOException {
            return Files.readAllBytes(path);
        }

        @Override
        public InputStream getInputStream()
                throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public void transferTo(File destination)
                throws IOException {

            Files.copy(
                    path,
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
