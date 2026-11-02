
package com.ecommerce.model;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_images_product"
            )
    )
    private Product product;

    @Column(
            name = "storage_key",
            nullable = false,
            length = 512,
            unique = true
    )
    private String storageKey;

    @Column(
            name = "original_filename",
            nullable = false,
            length = 255
    )
    private String originalFilename;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(
            name = "file_size",
            nullable = false
    )
    private long fileSize;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private int sortOrder;

    @Column(
            name = "is_primary",
            nullable = false
    )
    private boolean primary;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected ProductImage() {
        // Required by JPA
    }

    public ProductImage(
            Product product,
            String storageKey,
            String originalFilename,
            String contentType,
            long fileSize,
            int sortOrder,
            boolean primary
    ) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product is required"
            );
        }

        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Storage key is required"
            );
        }

        if (originalFilename == null ||
                originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "Original filename is required"
            );
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "Content type is required"
            );
        }

        if (fileSize <= 0) {
            throw new IllegalArgumentException(
                    "File size must be positive"
            );
        }

        if (sortOrder < 0) {
            throw new IllegalArgumentException(
                    "Sort order cannot be negative"
            );
        }

        this.product = product;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sortOrder = sortOrder;
        this.primary = primary;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isPrimary() {
        return primary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new IllegalArgumentException(
                    "Sort order cannot be negative"
            );
        }

        this.sortOrder = sortOrder;
    }

    public void markAsPrimary() {
        this.primary = true;
    }

    public void removePrimary() {
        this.primary = false;
    }
}
