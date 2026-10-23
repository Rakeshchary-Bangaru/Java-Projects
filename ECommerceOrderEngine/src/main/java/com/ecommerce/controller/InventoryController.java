package com.ecommerce.controller;

import com.ecommerce.dto.InventoryQuantityRequest;
import com.ecommerce.dto.InventoryResponse;

import com.ecommerce.service.SpringInventoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final SpringInventoryService inventoryService;

    public InventoryController(
            SpringInventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }


    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getStock(
            @PathVariable long productId) {

        int quantity =
                inventoryService.getStock(
                        productId
                );

        return ResponseEntity.ok(
                new InventoryResponse(
                        productId,
                        quantity
                )
        );
    }


    @PutMapping("/{productId}")
    public ResponseEntity<InventoryResponse> setStock(
            @PathVariable long productId,
            @Valid
            @RequestBody
            InventoryQuantityRequest request) {

        inventoryService.setStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok(
                new InventoryResponse(
                        productId,
                        inventoryService.getStock(
                                productId
                        )
                )
        );
    }


    @PostMapping("/{productId}/add")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable long productId,
            @Valid
            @RequestBody
            InventoryQuantityRequest request) {

        inventoryService.addStock(
                productId,
                request.getQuantity()
        );

        return ResponseEntity.ok(
                new InventoryResponse(
                        productId,
                        inventoryService.getStock(
                                productId
                        )
                )
        );
    }
}