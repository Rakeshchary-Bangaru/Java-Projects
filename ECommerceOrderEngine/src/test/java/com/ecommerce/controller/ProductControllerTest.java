package com.ecommerce.controller;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldReturnProductById() throws Exception {

        Product product =
                new Product(
                        7001,
                        "Laptop",
                        "Electronics",
                        75000
                );


        when(
                productService.findByIdOrThrow(7001)
        )
                .thenReturn(product);


        mockMvc.perform(
                        get("/api/products/7001")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Laptop")
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("Electronics")
                )
                .andExpect(
                        jsonPath("$.price")
                                .value(75000)
                );


        verify(productService)
                .findByIdOrThrow(7001);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        long productId = 9999L;


        when(
                productService.findByIdOrThrow(productId)
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Product not found"
                        )
                );


        mockMvc.perform(
                        get(
                                "/api/products/{id}",
                                productId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(productService)
                .findByIdOrThrow(productId);
    }

    @Test
    void shouldCreateProduct() throws Exception {

        Product savedProduct =
                new Product(
                        7001,
                        "Laptop",
                        "Electronics",
                        75000
                );


        when(
                productService.save(
                        any(Product.class)
                )
        )
                .thenReturn(savedProduct);


        String requestBody =
                """
                {
                  "id": 7001,
                  "name": "Laptop",
                  "category": "Electronics",
                  "price": 75000
                }
                """;


        mockMvc.perform(
                        post("/api/products")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Laptop")
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("Electronics")
                )
                .andExpect(
                        jsonPath("$.price")
                                .value(75000)
                );


        verify(productService)
                .save(
                        any(Product.class)
                );
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidProduct()
            throws Exception {

        String requestBody =
                """
                {
                  "id": 7001,
                  "name": "",
                  "category": "",
                  "price": -100
                }
                """;


        mockMvc.perform(
                        post("/api/products")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );


        verify(
                productService,
                never()
        )
                .save(
                        any(Product.class)
                );
    }

    @Test
    void shouldReplaceProduct() throws Exception {

        BigDecimal price =
                new BigDecimal("82000.00");


        Product updatedProduct =
                new Product(
                        7001L,
                        "Updated Laptop",
                        "Electronics",
                        price
                );


        when(
                productService.replace(
                        7001L,
                        "Updated Laptop",
                        "Electronics",
                        price
                )
        )
                .thenReturn(updatedProduct);


        String requestBody =
                """
                {
                  "id": 7001,
                  "name": "Updated Laptop",
                  "category": "Electronics",
                  "price": 82000.00
                }
                """;


        mockMvc.perform(
                        put("/api/products/7001")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Laptop")
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("Electronics")
                )
                .andExpect(
                        jsonPath("$.price")
                                .value(82000.00)
                );


        verify(productService)
                .replace(
                        7001L,
                        "Updated Laptop",
                        "Electronics",
                        price
                );
    }

    @Test
    void shouldUpdateProductPrice() throws Exception {

        BigDecimal newPrice =
                new BigDecimal("85000.00");


        Product updatedProduct =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        newPrice
                );


        when(
                productService.updatePrice(
                        7001L,
                        newPrice
                )
        )
                .thenReturn(updatedProduct);


        String requestBody =
                """
                {
                  "price": 85000.00
                }
                """;


        mockMvc.perform(
                        patch("/api/products/7001/price")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Laptop")
                )
                .andExpect(
                        jsonPath("$.category")
                                .value("Electronics")
                )
                .andExpect(
                        jsonPath("$.price")
                                .value(85000.00)
                );


        verify(productService)
                .updatePrice(
                        7001L,
                        newPrice
                );
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        long productId = 7001L;


        mockMvc.perform(
                        delete(
                                "/api/products/{id}",
                                productId
                        )
                )
                .andExpect(
                        status().isNoContent()
                );


        verify(productService)
                .deleteOrThrow(productId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingProduct()
            throws Exception {

        long productId = 9999L;


        doThrow(
                new ResourceNotFoundException(
                        "Product not found"
                )
        )
                .when(productService)
                .deleteOrThrow(productId);


        mockMvc.perform(
                        delete(
                                "/api/products/{id}",
                                productId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(productService)
                .deleteOrThrow(productId);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingInvalidPrice()
            throws Exception {

        String requestBody =
                """
                {
                  "price": -100
                }
                """;


        mockMvc.perform(
                        patch("/api/products/7001/price")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );


        verify(
                productService,
                never()
        )
                .updatePrice(
                        anyLong(),
                        any(BigDecimal.class)
                );
    }
}