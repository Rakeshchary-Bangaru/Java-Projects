
package com.ecommerce.controller;

import com.ecommerce.service.ProductImageService;
import com.ecommerce.service.ProductImageService.ProductImageResponse;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(
            ProductImageService imageService
    ) {
        this.imageService = imageService;
    }

    /*
     * Public image metadata.
     */
    @GetMapping
    public List<ProductImageResponse> listImages(
            @PathVariable long productId
    ) {
        return imageService.list(productId);
    }

    /*
     * Serve the image through Spring Boot.
     *
     * No filesystem path or internal storage key
     * is exposed to the browser.
     */
    @GetMapping("/{imageId}/content")
    public ResponseEntity<Resource> getImageContent(
            @PathVariable long productId,
            @PathVariable long imageId
    ) throws IOException {

        var content = imageService.getImage(
                productId,
                imageId
        );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                content.contentType()
                        )
                )
                .cacheControl(
                        CacheControl
                                .maxAge(1, TimeUnit.HOURS)
                                .cachePublic()
                )
                .body(content.resource());
    }

    /*
     * ROOT: all products.
     * SELLER_ADMIN: approved + owns product.
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImageResponse> upload(
            @PathVariable long productId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        ProductImageResponse image = imageService.upload(
                productId,
                file,
                authentication
        );

        return ResponseEntity
                .status(201)
                .body(image);
    }

    @PutMapping("/{imageId}/primary")
    public ProductImageResponse setPrimary(
            @PathVariable long productId,
            @PathVariable long imageId,
            Authentication authentication
    ) {

        return imageService.setPrimary(
                productId,
                imageId,
                authentication
        );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable long productId,
            @PathVariable long imageId,
            Authentication authentication
    ) {

        imageService.delete(
                productId,
                imageId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }
}
