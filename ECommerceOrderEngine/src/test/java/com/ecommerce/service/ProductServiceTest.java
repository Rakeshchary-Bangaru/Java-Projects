package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private SpringDataProductRepository productRepository;


    @InjectMocks
    private ProductService productService;


    /*
     * save()
     */
    @Test
    void shouldSaveProduct() {

        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("75000.00")
                );


        when(
                productRepository.save(product)
        )
                .thenReturn(product);


        Product result =
                productService.save(product);


        assertSame(
                product,
                result
        );


        verify(productRepository)
                .save(product);
    }


    /*
     * save(null)
     */
    @Test
    void shouldThrowExceptionWhenSavingNullProduct() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> productService.save(null)
                );


        assertEquals(
                "Product cannot be null",
                exception.getMessage()
        );


        verify(
                productRepository,
                never()
        )
                .save(any(Product.class));
    }


    /*
     * findByIdOrThrow()
     */
    @Test
    void shouldReturnProductWhenProductExists() {

        long productId = 7001L;


        Product product =
                new Product(
                        productId,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("75000.00")
                );


        when(
                productRepository.findById(productId)
        )
                .thenReturn(
                        Optional.of(product)
                );


        Product result =
                productService.findByIdOrThrow(
                        productId
                );


        assertSame(
                product,
                result
        );


        assertEquals(
                7001L,
                result.getId()
        );


        assertEquals(
                "Laptop",
                result.getName()
        );


        verify(productRepository)
                .findById(productId);
    }


    /*
     * findByIdOrThrow() missing product
     */
    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        long productId = 9999L;


        when(
                productRepository.findById(productId)
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                productService
                                        .findByIdOrThrow(
                                                productId
                                        )
                );


        assertEquals(
                "Product not found: 9999",
                exception.getMessage()
        );


        verify(productRepository)
                .findById(productId);
    }


    /*
     * findAll()
     */
    @Test
    void shouldReturnAllProducts() {

        Product laptop =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("75000.00")
                );


        Product mouse =
                new Product(
                        7401L,
                        "Wireless Mouse",
                        "Electronics",
                        new BigDecimal("2500.00")
                );


        List<Product> products =
                List.of(
                        laptop,
                        mouse
                );


        when(
                productRepository.findAll()
        )
                .thenReturn(products);


        List<Product> result =
                productService.findAll();


        assertEquals(
                2,
                result.size()
        );


        assertEquals(
                "Laptop",
                result.get(0).getName()
        );


        assertEquals(
                "Wireless Mouse",
                result.get(1).getName()
        );


        verify(productRepository)
                .findAll();
    }


    /*
     * update()
     */
    @Test
    void shouldUpdateProduct() {

        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("80000.00")
                );


        when(
                productRepository.save(product)
        )
                .thenReturn(product);


        Product result =
                productService.update(product);


        assertSame(
                product,
                result
        );


        verify(productRepository)
                .save(product);
    }


    /*
     * update(null)
     */
    @Test
    void shouldThrowExceptionWhenUpdatingNullProduct() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                productService.update(null)
                );


        assertEquals(
                "Product cannot be null",
                exception.getMessage()
        );


        verify(
                productRepository,
                never()
        )
                .save(any(Product.class));
    }


    /*
     * deleteById()
     */
    @Test
    void shouldDeleteProductById() {

        long productId = 7001L;


        productService.deleteById(
                productId
        );


        verify(productRepository)
                .deleteById(
                        productId
                );
    }


    /*
     * replace()
     */
    @Test
    void shouldReplaceExistingProduct() {

        long productId = 7001L;

        BigDecimal newPrice =
                new BigDecimal("82000.00");


        when(
                productRepository.existsById(productId)
        )
                .thenReturn(true);


        when(
                productRepository.save(
                        any(Product.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        Product result =
                productService.replace(
                        productId,
                        "Updated Laptop",
                        "Computers",
                        newPrice
                );


        assertEquals(
                productId,
                result.getId()
        );


        assertEquals(
                "Updated Laptop",
                result.getName()
        );


        assertEquals(
                "Computers",
                result.getCategory()
        );


        assertEquals(
                newPrice,
                result.getPriceAmount()
        );


        verify(productRepository)
                .existsById(productId);


        /*
         * ArgumentCaptor lets us inspect the Product
         * created inside ProductService.
         */
        ArgumentCaptor<Product> productCaptor =
                ArgumentCaptor.forClass(
                        Product.class
                );


        verify(productRepository)
                .save(
                        productCaptor.capture()
                );


        Product savedProduct =
                productCaptor.getValue();


        assertEquals(
                productId,
                savedProduct.getId()
        );


        assertEquals(
                "Updated Laptop",
                savedProduct.getName()
        );


        assertEquals(
                "Computers",
                savedProduct.getCategory()
        );


        assertEquals(
                newPrice,
                savedProduct.getPriceAmount()
        );
    }


    /*
     * replace() missing product
     */
    @Test
    void shouldThrowExceptionWhenReplacingMissingProduct() {

        long productId = 9999L;


        when(
                productRepository.existsById(productId)
        )
                .thenReturn(false);


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                productService.replace(
                                        productId,
                                        "Laptop",
                                        "Electronics",
                                        new BigDecimal(
                                                "75000.00"
                                        )
                                )
                );


        assertEquals(
                "Product not found: 9999",
                exception.getMessage()
        );


        verify(productRepository)
                .existsById(productId);


        verify(
                productRepository,
                never()
        )
                .save(any(Product.class));
    }


    /*
     * updatePrice()
     */
    @Test
    void shouldUpdateProductPrice() {

        long productId = 7001L;


        Product product =
                new Product(
                        productId,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("75000.00")
                );


        BigDecimal newPrice =
                new BigDecimal("85000.00");


        when(
                productRepository.findById(productId)
        )
                .thenReturn(
                        Optional.of(product)
                );


        Product result =
                productService.updatePrice(
                        productId,
                        newPrice
                );


        assertSame(
                product,
                result
        );


        assertEquals(
                newPrice,
                result.getPriceAmount()
        );


        verify(productRepository)
                .findById(productId);
    }


    /*
     * updatePrice() missing product
     */
    @Test
    void shouldThrowExceptionWhenUpdatingPriceOfMissingProduct() {

        long productId = 9999L;


        when(
                productRepository.findById(productId)
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                productService.updatePrice(
                                        productId,
                                        new BigDecimal(
                                                "85000.00"
                                        )
                                )
                );


        assertEquals(
                "Product not found: 9999",
                exception.getMessage()
        );


        verify(productRepository)
                .findById(productId);
    }


    /*
     * deleteOrThrow()
     */
    @Test
    void shouldDeleteExistingProduct() {

        long productId = 7001L;


        when(
                productRepository.existsById(productId)
        )
                .thenReturn(true);


        productService.deleteOrThrow(
                productId
        );


        verify(productRepository)
                .existsById(productId);


        verify(productRepository)
                .deleteById(productId);
    }


    /*
     * deleteOrThrow() missing product
     */
    @Test
    void shouldThrowExceptionWhenDeletingMissingProduct() {

        long productId = 9999L;


        when(
                productRepository.existsById(productId)
        )
                .thenReturn(false);


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                productService.deleteOrThrow(
                                        productId
                                )
                );


        assertEquals(
                "Product not found: 9999",
                exception.getMessage()
        );


        verify(productRepository)
                .existsById(productId);


        verify(
                productRepository,
                never()
        )
                .deleteById(productId);
    }
}