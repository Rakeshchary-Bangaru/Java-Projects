package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.seller.repository.SellerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private SpringDataProductRepository productRepository;

    @Mock
    private SellerRepository sellerRepository;

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

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "root@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ROOT"
                                )
                        )
                );

        when(
                productRepository.save(product)
        ).thenReturn(product);

        Product result =
                productService.save(
                        product,
                        authentication
                );

        assertSame(
                product,
                result
        );

        verify(productRepository)
                .save(product);

        /*
         * ROOT does not need a Seller profile.
         */
        verifyNoInteractions(
                sellerRepository
        );
    }


    /*
     * save(null)
     */
    @Test
    void shouldThrowExceptionWhenSavingNullProduct() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                productService.save(
                                        null,
                                        null
                                )
                );

        assertEquals(
                "Product cannot be null",
                exception.getMessage()
        );

        verify(
                productRepository,
                never()
        ).save(
                any(Product.class)
        );

        verifyNoInteractions(
                sellerRepository
        );
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
        ).thenReturn(
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
        ).thenReturn(
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
        ).thenReturn(products);

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

    /*
     * update(null)
     */


    /*
     * replace()
     */
    @Test
    void shouldReplaceExistingProduct() {

        long productId = 7001L;

        Product existingProduct =
                new Product(
                        productId,
                        "Old Product",
                        "Old Category",
                        new BigDecimal("100.00")
                );

        when(
                productRepository.findById(productId)
        ).thenReturn(
                Optional.of(existingProduct)
        );

        Product result =
                productService.replace(
                        productId,
                        "Updated Product",
                        "Electronics",
                        new BigDecimal("250.00")
                );

        assertSame(
                existingProduct,
                result
        );

        assertEquals(
                "Updated Product",
                result.getName()
        );

        assertEquals(
                "Electronics",
                result.getCategory()
        );

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                result.getPriceAmount()
                        )
        );

        verify(productRepository)
                .findById(productId);

        verify(
                productRepository,
                never()
        ).existsById(
                anyLong()
        );

        verify(
                productRepository,
                never()
        ).save(
                any(Product.class)
        );
    }


    /*
     * replace() missing product
     */
    @Test
    void shouldThrowExceptionWhenReplacingMissingProduct() {

        long productId = 9999L;

        when(
                productRepository.findById(productId)
        ).thenReturn(
                Optional.empty()
        );

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
                .findById(productId);

        verify(
                productRepository,
                never()
        ).existsById(productId);

        verify(
                productRepository,
                never()
        ).save(
                any(Product.class)
        );
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
        ).thenReturn(
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
        ).thenReturn(
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
        ).thenReturn(true);

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
        ).thenReturn(false);

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
        ).deleteById(productId);
    }
}