
package com.ecommerce.migration;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!test")
@ConditionalOnProperty(
        name = "app.catalog.seed.enabled",
        havingValue = "true"
)
public class DemoCatalogueSeeder implements ApplicationRunner {

    private final SpringDataProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactions;

    public DemoCatalogueSeeder(
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
            String price,
            String description,
            String brand,
            int stock
    ) {}

    private static final List<CatalogueItem> CATALOGUE = List.of(

            // ELECTRONICS
            new CatalogueItem(
                    "Headphones", "Electronics", "2999.00",
                    "Wireless over-ear headphones",
                    "Demo Audio", 25
            ),

            // HOME APPLIANCES
            new CatalogueItem(
                    "Refrigerator", "Home Appliances", "34999.00",
                    "Double-door energy-efficient refrigerator",
                    "Demo Home", 8
            ),
            new CatalogueItem(
                    "Washing Machine", "Home Appliances", "26999.00",
                    "Fully automatic front-load washing machine",
                    "Demo Home", 10
            ),
            new CatalogueItem(
                    "Microwave", "Home Appliances", "6999.00",
                    "Compact microwave oven",
                    "Demo Home", 16
            ),

            // MOBILES
            new CatalogueItem(
                    "Smartphone", "Mobiles", "29999.00",
                    "5G smartphone with high-resolution display",
                    "Demo Mobile", 30
            ),
            new CatalogueItem(
                    "Tablet", "Mobiles", "19999.00",
                    "Portable tablet for entertainment and study",
                    "Demo Mobile", 20
            ),
            new CatalogueItem(
                    "Smartwatch", "Mobiles", "3999.00",
                    "Smartwatch with activity tracking",
                    "Demo Wearables", 35
            ),

            // FASHION
            new CatalogueItem(
                    "T-Shirt", "Fashion", "799.00",
                    "Comfortable cotton crew-neck T-shirt",
                    "Demo Fashion", 50
            ),
            new CatalogueItem(
                    "Jeans", "Fashion", "1499.00",
                    "Classic denim jeans",
                    "Demo Fashion", 40
            ),
            new CatalogueItem(
                    "Sneakers", "Fashion", "2999.00",
                    "Comfortable everyday sneakers",
                    "Demo Footwear", 25
            ),

            // BOOKS
            new CatalogueItem(
                    "Java Programming Book", "Books", "899.00",
                    "Introduction to Java and object-oriented programming",
                    "Demo Books", 45
            ),
            new CatalogueItem(
                    "Spring Boot Book", "Books", "999.00",
                    "Building REST APIs and applications with Spring Boot",
                    "Demo Books", 30
            ),
            new CatalogueItem(
                    "DSA Book", "Books", "799.00",
                    "Data structures and algorithms practice guide",
                    "Demo Books", 40
            ),

            // SPORTS
            new CatalogueItem(
                    "Football", "Sports", "1299.00",
                    "Durable football for recreational play",
                    "Demo Sports", 30
            ),
            new CatalogueItem(
                    "Cricket Bat", "Sports", "1499.00",
                    "Cricket bat for practice and recreational matches",
                    "Demo Sports", 20
            ),
            new CatalogueItem(
                    "Yoga Mat", "Sports", "599.00",
                    "Non-slip exercise and yoga mat",
                    "Demo Fitness", 35
            )
    );

    @Override
    public void run(ApplicationArguments args) {

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        // Safety: this maintenance job must never seed
        // test or production databases.
        if (!"ecommerce_db".equals(database)) {
            throw new IllegalStateException(
                    "Catalogue seed is restricted to ecommerce_db. " +
                            "Connected database: " + database
            );
        }

        String applyFlag = jdbcTemplate
                .getDataSource() == null
                ? null
                : "available";

        // The flag is checked below via application arguments.
        boolean apply = args.containsOption(
                "app.catalog.seed.apply"
        ) && args.getOptionValues(
                "app.catalog.seed.apply"
        ).contains("true");

        int newProducts = 0;
        int skipped = 0;

        for (CatalogueItem item : CATALOGUE) {

            boolean exists = productRepository.findAll()
                    .stream()
                    .anyMatch(product ->
                            product.getName()
                                    .equalsIgnoreCase(item.name())
                                    && product.getCategory()
                                    .equalsIgnoreCase(item.category())
                    );

            if (exists) {
                skipped++;
                System.out.println(
                        "SKIP existing: " + item.name()
                );
                continue;
            }

            if (!apply) {
                System.out.println(
                        "DRY RUN: " + item.category()
                                + " | " + item.name()
                                + " | ₹" + item.price()
                                + " | Stock: " + item.stock()
                );
                continue;
            }

            Long productId = transactions.execute(status -> {

                Product product = new Product(
                        item.name(),
                        item.category(),
                        new BigDecimal(item.price()),
                        item.description(),
                        item.brand()
                );

                Product saved =
                        productRepository.saveAndFlush(product);

                // Insert initial stock without exposing
                // inventory quantities to customers.
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

            newProducts++;

            System.out.println(
                    "CREATED: " + productId
                            + " | " + item.category()
                            + " | " + item.name()
            );
        }

        System.out.println("============================");
        System.out.println("Catalogue items: " + CATALOGUE.size());
        System.out.println("Existing skipped: " + skipped);
        System.out.println("New products created: " + newProducts);
        System.out.println("Apply mode: " + apply);
        System.out.println("============================");
    }
}
