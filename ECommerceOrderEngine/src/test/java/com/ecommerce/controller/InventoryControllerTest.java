package com.ecommerce.controller;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.SpringInventoryService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private SpringInventoryService inventoryService;


    /*
     * GET /api/inventory/{productId}
     */
    @Test
    void shouldReturnProductStock()
            throws Exception {

        long productId = 7001L;


        when(
                inventoryService.getStock(productId)
        )
                .thenReturn(20);


        mockMvc.perform(
                        get(
                                "/api/inventory/{productId}",
                                productId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.productId")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(20)
                );


        verify(inventoryService)
                .getStock(productId);
    }


    /*
     * GET missing product -> 404
     */
    @Test
    void shouldReturnNotFoundWhenGettingStockForMissingProduct()
            throws Exception {

        long productId = 9999L;


        when(
                inventoryService.getStock(productId)
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Product not found"
                        )
                );


        mockMvc.perform(
                        get(
                                "/api/inventory/{productId}",
                                productId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(inventoryService)
                .getStock(productId);
    }


    /*
     * PUT /api/inventory/{productId}
     */
    @Test
    void shouldSetProductStock()
            throws Exception {

        long productId = 7001L;


        when(
                inventoryService.getStock(productId)
        )
                .thenReturn(50);


        String requestBody =
                """
                {
                  "quantity": 50
                }
                """;


        mockMvc.perform(
                        put(
                                "/api/inventory/{productId}",
                                productId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.productId")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(50)
                );


        verify(inventoryService)
                .setStock(
                        productId,
                        50
                );


        verify(inventoryService)
                .getStock(productId);
    }


    /*
     * PUT can set stock to zero
     */
    @Test
    void shouldSetProductStockToZero()
            throws Exception {

        long productId = 7001L;


        when(
                inventoryService.getStock(productId)
        )
                .thenReturn(0);


        String requestBody =
                """
                {
                  "quantity": 0
                }
                """;


        mockMvc.perform(
                        put(
                                "/api/inventory/{productId}",
                                productId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(0)
                );


        verify(inventoryService)
                .setStock(
                        productId,
                        0
                );
    }


    /*
     * POST /api/inventory/{productId}/add
     */
    @Test
    void shouldAddProductStock()
            throws Exception {

        long productId = 7001L;


        when(
                inventoryService.getStock(productId)
        )
                .thenReturn(30);


        String requestBody =
                """
                {
                  "quantity": 10
                }
                """;


        mockMvc.perform(
                        post(
                                "/api/inventory/{productId}/add",
                                productId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.productId")
                                .value(7001)
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(30)
                );


        verify(inventoryService)
                .addStock(
                        productId,
                        10
                );


        verify(inventoryService)
                .getStock(productId);
    }


    /*
     * PUT missing product -> 404
     */
    @Test
    void shouldReturnNotFoundWhenSettingStockForMissingProduct()
            throws Exception {

        long productId = 9999L;


        doThrow(
                new ResourceNotFoundException(
                        "Product not found"
                )
        )
                .when(inventoryService)
                .setStock(
                        productId,
                        20
                );


        String requestBody =
                """
                {
                  "quantity": 20
                }
                """;


        mockMvc.perform(
                        put(
                                "/api/inventory/{productId}",
                                productId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(inventoryService)
                .setStock(
                        productId,
                        20
                );
    }


    /*
     * POST add stock for missing product -> 404
     */
    @Test
    void shouldReturnNotFoundWhenAddingStockForMissingProduct()
            throws Exception {

        long productId = 9999L;


        doThrow(
                new ResourceNotFoundException(
                        "Product not found"
                )
        )
                .when(inventoryService)
                .addStock(
                        productId,
                        10
                );


        String requestBody =
                """
                {
                  "quantity": 10
                }
                """;


        mockMvc.perform(
                        post(
                                "/api/inventory/{productId}/add",
                                productId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(inventoryService)
                .addStock(
                        productId,
                        10
                );
    }
}