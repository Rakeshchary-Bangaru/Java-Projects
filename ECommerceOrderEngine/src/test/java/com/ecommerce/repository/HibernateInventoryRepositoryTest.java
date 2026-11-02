
package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HibernateInventoryRepositoryTest {

    @Test
    void shouldManageAndReserveInventoryAtomically()
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        InventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        /*
         * Product ID is generated automatically by MySQL.
         * No manually assigned product ID is required.
         */
        Product product = new Product(
                "Hibernate Inventory Product",
                "Testing",
                new BigDecimal("25.00"),
                "Product used for inventory repository testing",
                "TestBrand"
        );

        Long productId = null;

        try {

            /*
             * =================================================
             * SAVE PRODUCT
             * =================================================
             */
            productRepository.save(product);

            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            /*
             * =================================================
             * SET INITIAL STOCK
             * =================================================
             */
            inventoryRepository.setStock(
                    productId,
                    10
            );

            assertEquals(
                    10,
                    inventoryRepository.getStock(productId)
            );

            /*
             * =================================================
             * SUCCESSFUL STOCK RESERVATION
             * =================================================
             */
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

            /*
             * =================================================
             * FAILED STOCK RESERVATION
             * =================================================
             *
             * Available stock = 7.
             * Requested stock = 100.
             */
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

            /*
             * =================================================
             * RELEASE STOCK
             * =================================================
             */
            inventoryRepository.releaseStock(
                    productId,
                    2
            );

            assertEquals(
                    9,
                    inventoryRepository.getStock(productId)
            );

            /*
             * =================================================
             * ADD STOCK
             * =================================================
             */
            inventoryRepository.addStock(
                    productId,
                    5
            );

            assertEquals(
                    14,
                    inventoryRepository.getStock(productId)
            );

            /*
             * =================================================
             * REPLACE STOCK
             * =================================================
             */
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
             * Inventory references Product through a foreign
             * key, so inventory must be deleted first.
             *
             * If product persistence fails, productId
             * remains null and cleanup is skipped.
             */
            if (productId != null) {

                deleteInventory(productId);

                productRepository.deleteById(productId);
            }
        }
    }

    /*
     * =========================================================
     * DELETE TEST INVENTORY
     * =========================================================
     *
     * Keep rollback inside the open Hibernate session.
     */
    private void deleteInventory(long productId) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {

                session.createMutationQuery(
                                """
                                delete from Inventory i
                                where i.productId = :productId
                                """
                        )
                        .setParameter("productId", productId)
                        .executeUpdate();

                transaction.commit();

            } catch (RuntimeException e) {

                try {

                    if (transaction.isActive()) {
                        transaction.rollback();
                    }

                } catch (RuntimeException rollbackException) {

                    e.addSuppressed(rollbackException);
                }

                throw e;
            }
        }
    }
}
