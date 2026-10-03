package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;
import com.ecommerce.repository.HibernateInventoryRepository;
import com.ecommerce.repository.HibernateProductRepository;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.ProductRepository;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateInventoryRepositoryDemo {

    public static void main(String[] args) throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        InventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        long productId = 930001L;

        /*
         * Inventory.product_id is a foreign key to products.id,
         * so create a temporary product first.
         */
        Product product =
                new Product(
                        productId,
                        "Inventory Test Product",
                        "Testing",
                        49.99
                );

        productRepository.save(product);

        System.out.println("PRODUCT CREATED");


        // 1. SET STOCK
        inventoryRepository.setStock(
                productId,
                10
        );

        System.out.println(
                "INITIAL STOCK: "
                        + inventoryRepository.getStock(productId)
        );


        // 2. SUCCESSFUL RESERVATION
        boolean reserved =
                inventoryRepository.reserveStock(
                        productId,
                        3
                );

        System.out.println(
                "RESERVE 3: " + reserved
        );

        System.out.println(
                "STOCK AFTER RESERVE: "
                        + inventoryRepository.getStock(productId)
        );


        // 3. FAILED RESERVATION
        boolean failedReservation =
                inventoryRepository.reserveStock(
                        productId,
                        100
                );

        System.out.println(
                "RESERVE 100: "
                        + failedReservation
        );

        System.out.println(
                "STOCK AFTER FAILED RESERVE: "
                        + inventoryRepository.getStock(productId)
        );


        // 4. RELEASE STOCK
        inventoryRepository.releaseStock(
                productId,
                2
        );

        System.out.println(
                "STOCK AFTER RELEASE 2: "
                        + inventoryRepository.getStock(productId)
        );


        // 5. ADD STOCK
        inventoryRepository.addStock(
                productId,
                5
        );

        System.out.println(
                "STOCK AFTER ADD 5: "
                        + inventoryRepository.getStock(productId)
        );


        // 6. REPLACE STOCK WITH EXACT VALUE
        inventoryRepository.setStock(
                productId,
                4
        );

        System.out.println(
                "STOCK AFTER SET TO 4: "
                        + inventoryRepository.getStock(productId)
        );


        /*
         * Cleanup test inventory before deleting the Product,
         * because inventory.product_id references products.id.
         */
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.createMutationQuery(
                            """
                            delete from Inventory i
                            where i.productId = :productId
                            """
                    )
                    .setParameter(
                            "productId",
                            productId
                    )
                    .executeUpdate();

            transaction.commit();
        }

        productRepository.deleteById(productId);

        System.out.println("TEST DATA CLEANED UP");

        HibernateUtil.shutdown();
    }
}