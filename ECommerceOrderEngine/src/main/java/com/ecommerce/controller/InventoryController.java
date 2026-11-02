package com.ecommerce.controller;

import com.ecommerce.dto.InventoryQuantityRequest;
import com.ecommerce.dto.InventoryResponse;
import com.ecommerce.dto.ProductAvailabilityResponse;

import com.ecommerce.service.SpringInventoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final SpringInventoryService inventoryService;


    public InventoryController(
            SpringInventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }


    /*
     * Public/customer-safe availability endpoint.
     *
     * Does NOT expose exact stock quantity.
     */
    @GetMapping("/{productId}/availability")
    public ResponseEntity<ProductAvailabilityResponse>
    getAvailability(
            @PathVariable long productId
    ) {

        int quantity =
                inventoryService.getStock(
                        productId
                );

        boolean inStock =
                quantity > 0;

        ProductAvailabilityResponse response =
                new ProductAvailabilityResponse(
                        productId,
                        inStock,
                        inStock
                                ? "IN_STOCK"
                                : "OUT_OF_STOCK"
                );

        return ResponseEntity.ok(
                response
        );
    }


    /*
     * Exact inventory quantity.
     *
     * ROOT:
     * - can view any product inventory
     *
     * SELLER_ADMIN:
     * - must own the product
     *
     * CUSTOMER / IT_SUPPORT:
     * - denied
     */
    @GetMapping("/{productId}")
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('INVENTORY_UPDATE_OWN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    public ResponseEntity<InventoryResponse> getStock(
            @PathVariable long productId
    ) {

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


    /*
     * Set exact stock quantity.
     */
    @PutMapping("/{productId}")
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('INVENTORY_UPDATE_OWN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    public ResponseEntity<InventoryResponse> setStock(
            @PathVariable long productId,
            @Valid
            @RequestBody
            InventoryQuantityRequest request
    ) {

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


    /*
     * Add stock.
     */
    @PostMapping("/{productId}/add")
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('INVENTORY_UPDATE_OWN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable long productId,
            @Valid
            @RequestBody
            InventoryQuantityRequest request
    ) {

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