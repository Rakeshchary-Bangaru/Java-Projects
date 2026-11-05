
package com.ecommerce.migration;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Profile("!test")
@ConditionalOnProperty(
        name = "app.catalog.expansion.enabled",
        havingValue = "true"
)
public class DemoCatalogueExpansionRunner
        implements ApplicationRunner {

    private final SpringDataProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactions;

    @Value("${app.catalog.expansion.apply:false}")
    private boolean apply;

    @Value("${app.catalog.expansion.csv:./migration-assets/catalogue/v11-catalogue-expansion-42.csv}")
    private String csvPath;

    public DemoCatalogueExpansionRunner(
            SpringDataProductRepository productRepository,
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactions
    ) {
        this.productRepository = productRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactions = transactions;
    }

    private record CatalogueItem(
            String name,
            String category,
            BigDecimal price,
            String description,
            String brand,
            int stock
    ) {}

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // ==================================================
        // 1. VERIFY LOCAL DATABASE
        // ==================================================

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        if (!"ecommerce_db".equals(database)) {
            throw new IllegalStateException(
                    "Catalogue expansion restricted to " +
                            "ecommerce_db. Connected database: " +
                            database
            );
        }

        if (jdbcTemplate.getDataSource() == null) {
            throw new IllegalStateException(
                    "DataSource is unavailable"
            );
        }

        String jdbcUrl;

        try (var connection =
                     jdbcTemplate.getDataSource().getConnection()) {
            jdbcUrl = connection.getMetaData().getURL();
        }

        if (jdbcUrl == null) {
            throw new IllegalStateException(
                    "Unable to verify database connection URL"
            );
        }

        boolean localDatabase =
                jdbcUrl.startsWith("jdbc:mysql://localhost:")
                        || jdbcUrl.startsWith("jdbc:mysql://127.0.0.1:");

        if (!localDatabase) {
            throw new IllegalStateException(
                    "Catalogue expansion requires local MySQL. " +
                            "Remote databases are not allowed."
            );
        }

        // ==================================================
        // 2. LOAD AND VALIDATE CSV
        // ==================================================

        List<CatalogueItem> catalogue = readCatalogue();

        System.out.println();
        System.out.println("=================================");
        System.out.println(" V11 CATALOGUE EXPANSION");
        System.out.println("=================================");
        System.out.println("Database: " + database);
        System.out.println("CSV: " + csvPath);
        System.out.println("CSV products: " + catalogue.size());
        System.out.println("Apply mode: " + apply);
        System.out.println("=================================");

        // ==================================================
        // 3. READ EXISTING PRODUCTS
        // ==================================================

        Set<String> existingProducts = new HashSet<>();

        for (Product product : productRepository.findAll()) {
            existingProducts.add(
                    productKey(
                            product.getName(),
                            product.getCategory()
                    )
            );
        }

        int created = 0;
        int skipped = 0;
        int wouldCreate = 0;

        // ==================================================
        // 4. PROCESS NEW PRODUCTS
        // ==================================================

        for (CatalogueItem item : catalogue) {

            String key = productKey(
                    item.name(),
                    item.category()
            );

            if (existingProducts.contains(key)) {
                skipped++;

                System.out.println(
                        "SKIP existing: " + item.name()
                );

                continue;
            }

            // DRY RUN: No database changes.
            if (!apply) {
                wouldCreate++;

                System.out.println(
                        "DRY RUN: " +
                                item.category() +
                                " | " + item.name() +
                                " | ₹" + item.price() +
                                " | Stock: " + item.stock()
                );

                continue;
            }

            // APPLY: Product and inventory are created
            // together inside one transaction.
            Long productId = transactions.execute(status -> {

                Product product = new Product(
                        item.name(),
                        item.category(),
                        item.price(),
                        item.description(),
                        item.brand()
                );

                Product saved =
                        productRepository.saveAndFlush(product);

                jdbcTemplate.update(
                        """
                        INSERT INTO inventory
                            (product_id, quantity)
                        VALUES (?, ?)
                        """,
                        saved.getId(),
                        item.stock()
                );

                return saved.getId();
            });

            existingProducts.add(key);
            created++;

            System.out.println(
                    "CREATED: " + productId +
                            " | " + item.category() +
                            " | " + item.name()
            );
        }

        // ==================================================
        // 5. SUMMARY
        // ==================================================

        System.out.println();
        System.out.println("=================================");
        System.out.println(" EXPANSION SUMMARY");
        System.out.println("=================================");
        System.out.println("Catalogue items: " + catalogue.size());
        System.out.println("Existing skipped: " + skipped);
        System.out.println("Would create: " + wouldCreate);
        System.out.println("New products created: " + created);
        System.out.println("Apply mode: " + apply);
        System.out.println("=================================");
    }

    private List<CatalogueItem> readCatalogue()
            throws Exception {

        Path path = Path.of(csvPath);

        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException(
                    "Catalogue CSV not found: " +
                            path.toAbsolutePath()
            );
        }

        List<String> lines = Files.readAllLines(path);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Catalogue CSV is empty"
            );
        }

        String expectedHeader =
                "name,category,price,description,brand,stock";

        String header = lines.get(0)
                .replace("\uFEFF", "")
                .trim();

        if (!expectedHeader.equals(header)) {
            throw new IllegalArgumentException(
                    "Invalid CSV header. Expected: " +
                            expectedHeader
            );
        }

        List<CatalogueItem> catalogue = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            // Suitable for our fixed catalogue CSV:
            // fields do not contain embedded commas.
            String[] values = line.split(",", -1);

            if (values.length != 6) {
                throw new IllegalArgumentException(
                        "Invalid CSV format at line " + (i + 1)
                );
            }

            String name = values[0].trim();
            String category = values[1].trim();
            BigDecimal price =
                    new BigDecimal(values[2].trim());
            String description = values[3].trim();
            String brand = values[4].trim();
            int stock = Integer.parseInt(values[5].trim());

            if (name.isBlank()
                    || category.isBlank()
                    || description.isBlank()
                    || brand.isBlank()
                    || price.signum() <= 0
                    || stock < 0) {

                throw new IllegalArgumentException(
                        "Invalid product at line " + (i + 1)
                );
            }

            String key = productKey(name, category);

            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate CSV product: " + name
                );
            }

            catalogue.add(
                    new CatalogueItem(
                            name,
                            category,
                            price,
                            description,
                            brand,
                            stock
                    )
            );
        }

        if (catalogue.size() != 42) {
            throw new IllegalArgumentException(
                    "Expected 42 products but found " +
                            catalogue.size()
            );
        }

        return catalogue;
    }

    private String productKey(
            String name,
            String category
    ) {
        return name.trim().toLowerCase(Locale.ROOT)
                + "|"
                + category.trim().toLowerCase(Locale.ROOT);
    }
}
