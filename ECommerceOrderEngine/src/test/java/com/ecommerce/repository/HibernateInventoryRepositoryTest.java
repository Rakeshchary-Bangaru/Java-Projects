package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HibernateInventoryRepositoryTest {

    @Test
    void shouldManageAndReserveInventoryAtomically()
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        InventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        long productId = 980201L;

        /*
         * Clean any data left by an interrupted previous run.
         */
        deleteInventory(productId);
        productRepository.deleteById(productId);

        Product product =
                new Product(
                        productId,
                        "Hibernate Inventory Product",
                        "Testing",
                        25.00
                );

        try {

            productRepository.save(product);

            // SET STOCK
            inventoryRepository.setStock(
                    productId,
                    10
            );

            assertEquals(
                    10,
                    inventoryRepository.getStock(productId)
            );


            // SUCCESSFUL RESERVATION
            boolean reserved =
                    inventoryRepository.reserveStock(
                            productId,
                            3
                    );

            assertTrue(reserved);

            assertEquals(
                    7,
                    inventoryRepository.getStock(productId)
            );


            // FAILED RESERVATION
            boolean overReserved =
                    inventoryRepository.reserveStock(
                            productId,
                            100
                    );

            assertFalse(overReserved);

            /*
             * Failed reservation must not reduce stock.
             */
            assertEquals(
                    7,
                    inventoryRepository.getStock(productId)
            );


            // RELEASE STOCK
            inventoryRepository.releaseStock(
                    productId,
                    2
            );

            assertEquals(
                    9,
                    inventoryRepository.getStock(productId)
            );


            // ADD STOCK
            inventoryRepository.addStock(
                    productId,
                    5
            );

            assertEquals(
                    14,
                    inventoryRepository.getStock(productId)
            );


            // REPLACE STOCK
            inventoryRepository.setStock(
                    productId,
                    4
            );

            assertEquals(
                    4,
                    inventoryRepository.getStock(productId)
            );

        } finally {

            /*
             * Inventory references Product through a foreign key,
             * so remove inventory before deleting the product.
             */
            deleteInventory(productId);

            productRepository.deleteById(
                    productId
            );
        }
    }


    private void deleteInventory(
            long productId
    ) {

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

        } catch (RuntimeException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;
        }
    }
}