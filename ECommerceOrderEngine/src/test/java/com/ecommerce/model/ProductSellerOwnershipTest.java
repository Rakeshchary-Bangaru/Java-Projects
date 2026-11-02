package com.ecommerce.model;

import com.ecommerce.seller.model.Seller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProductSellerOwnershipTest {

    @Test
    void existingProductShouldBeAllowedWithoutSeller() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        80000
                );

        assertNull(
                product.getSeller()
        );
    }


    @Test
    void productShouldBeAssignableToSeller() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        80000
                );

        Seller seller =
                mock(Seller.class);

        product.assignSeller(seller);

        assertSame(
                seller,
                product.getSeller()
        );
    }
}