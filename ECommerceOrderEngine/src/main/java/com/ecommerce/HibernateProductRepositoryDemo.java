package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;
import com.ecommerce.repository.HibernateProductRepository;
import com.ecommerce.repository.ProductRepository;

public class HibernateProductRepositoryDemo {

    public static void main(String[] args) throws Exception {

        ProductRepository repository =
                new HibernateProductRepository();

        long productId = 910001L;

        // 1. SAVE
        Product product =
                new Product(
                        productId,
                        "Hibernate Keyboard",
                        "Electronics",
                        89.99
                );

        repository.save(product);

        System.out.println("SAVE completed");


        // 2. FIND BY ID
        repository.findById(productId)
                .ifPresent(found ->
                        System.out.println(
                                "FOUND: "
                                        + found.getName()
                                        + " - "
                                        + found.getPrice()
                        )
                );


        // 3. FIND ALL
        System.out.println(
                "Total products: "
                        + repository.findAll().size()
        );


        // 4. UPDATE
        Product existing =
                repository.findById(productId)
                        .orElseThrow();

        existing.updatePrice(99.99);

        repository.update(existing);

        Product updated =
                repository.findById(productId)
                        .orElseThrow();

        System.out.println(
                "UPDATED PRICE: "
                        + updated.getPrice()
        );


        // 5. DELETE
        boolean deleted =
                repository.deleteById(productId);

        System.out.println(
                "DELETED: " + deleted
        );


        // Verify deletion
        System.out.println(
                "EXISTS AFTER DELETE: "
                        + repository
                        .findById(productId)
                        .isPresent()
        );

        HibernateUtil.shutdown();
    }
}