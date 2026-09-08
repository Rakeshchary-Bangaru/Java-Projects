package com.ecommerce.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest{
    private Product product;

    @BeforeEach
    void setUp(){
        product = new Product(1,"iPhone","Electronics",80000);
    }
    @Test
    void shouldCreateValidProduct(){
        assertAll(
                () -> assertEquals(1,product.getId()),
                () -> assertEquals("iPhone",product.getName()),
                () -> assertEquals("Electronics",product.getCategory()),
                () -> assertEquals(80000,product.getPrice())
        );
    }

    @Test
    void shouldUpdatePrice(){
        product.updatePrice(75000);
        assertEquals(75000,product.getPrice());
    }
    @ParameterizedTest
    @ValueSource(longs = {0,-1,-10})
    void shouldRejectInvalidProductId(long invalidId){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        invalidId,
                        "iPhone",
                        "Electronics",
                        80000
                )
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"" , " " , "  "})
    void shouldRejectInvalidProductName(String invalidName){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        1,
                        invalidName,
                        "Electronics",
                        80000
                )
        );
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"" , " " , "  "})
    void shouldRejectInvalidCategory(String invalidCategory){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        1,
                        "iPhone",
                        invalidCategory,
                        80000
                )
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {0,-1,-1000})
    void shouldRejectInvalidPrice(double invalidPrice){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        invalidPrice
                        )
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {0,-1,-500})
    void shouldRejectInvalidPriceUpdate(double invalidPrice){
        assertThrows(
                IllegalArgumentException.class,
                () -> product.updatePrice(invalidPrice)
        );
    }

}