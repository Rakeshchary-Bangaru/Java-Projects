
package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.model.ProductImage;
import com.ecommerce.seller.model.SellerApprovalStatus;

import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.repository.SpringDataProductRepository;

import com.ecommerce.storage.ProductImageStorageService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ProductImageService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    private final SpringDataProductRepository productRepository;
    private final SpringDataProductImageRepository imageRepository;
    private final SellerRepository sellerRepository;
    private final ProductImageStorageService storageService;
    private final EntityManager entityManager;

    public ProductImageService(
            SpringDataProductRepository productRepository,
            SpringDataProductImageRepository imageRepository,
            SellerRepository sellerRepository,
            ProductImageStorageService storageService,
            EntityManager entityManager
    ) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.sellerRepository = sellerRepository;
        this.storageService = storageService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> list(long productId) {

        requireProduct(productId);

        return imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImageContent getImage(
            long productId,
            long imageId
    ) throws IOException {

        ProductImage image = imageRepository
                .findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product image not found"
                        )
                );

        Resource resource =
                storageService.load(image.getStorageKey());

        return new ImageContent(
                resource,
                image.getContentType()
        );
    }

    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasRole('SELLER_ADMIN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    @Transactional(rollbackFor = Exception.class)
    public ProductImageResponse upload(
            long productId,
            MultipartFile file,
            Authentication authentication
    ) throws IOException {

        verifyApprovedSellerOrRoot(authentication);

        Product product = lockProduct(productId);

        List<ProductImage> existing =
                imageRepository
                        .findByProduct_IdOrderBySortOrderAscIdAsc(productId);

        if (existing.size() >= MAX_IMAGES_PER_PRODUCT) {
            throw new IllegalArgumentException(
                    "Maximum 8 images are allowed per product"
            );
        }

        var stored = storageService.store(file);

        /*
         * If the database transaction rolls back,
         * remove the newly uploaded file.
         */
        registerRollbackDeletion(stored.storageKey());

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "image";
        }

        // Metadata only; never use a client filename as a path.
        filename = filename.replace('\\', '/');
        filename = filename.substring(
                filename.lastIndexOf('/') + 1
        );

        if (filename.length() > 255) {
            filename = filename.substring(0, 255);
        }

        boolean firstImage = existing.isEmpty();

        int nextPosition = existing.stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        ProductImage image = new ProductImage(
                product,
                stored.storageKey(),
                filename,
                stored.contentType(),
                stored.fileSize(),
                nextPosition,
                firstImage
        );

        ProductImage saved = imageRepository.saveAndFlush(image);

        return toResponse(saved);
    }

    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasRole('SELLER_ADMIN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    @Transactional
    public ProductImageResponse setPrimary(
            long productId,
            long imageId,
            Authentication authentication
    ) {

        verifyApprovedSellerOrRoot(authentication);

        lockProduct(productId);

        ProductImage selected = imageRepository
                .findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product image not found"
                        )
                );

        List<ProductImage> images =
                imageRepository
                        .findByProduct_IdOrderBySortOrderAscIdAsc(productId);

        /*
         * Flush the removal of the previous primary flag
         * before assigning a new primary flag, so the
         * unique database constraint is not violated.
         */
        for (ProductImage image : images) {
            if (image.isPrimary()) {
                image.removePrimary();
            }
        }

        imageRepository.flush();

        selected.markAsPrimary();

        imageRepository.flush();

        return toResponse(selected);
    }

    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasRole('SELLER_ADMIN') && " +
                    "@productAuth.isOwner(#productId, authentication))"
    )
    @Transactional
    public void delete(
            long productId,
            long imageId,
            Authentication authentication
    ) {

        verifyApprovedSellerOrRoot(authentication);

        lockProduct(productId);

        ProductImage image = imageRepository
                .findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product image not found"
                        )
                );

        boolean wasPrimary = image.isPrimary();
        String storageKey = image.getStorageKey();

        imageRepository.delete(image);
        imageRepository.flush();

        if (wasPrimary) {

            List<ProductImage> remaining =
                    imageRepository
                            .findByProduct_IdOrderBySortOrderAscIdAsc(productId);

            if (!remaining.isEmpty()) {
                ProductImage next = remaining.get(0);
                next.markAsPrimary();
                imageRepository.flush();
            }
        }

        /*
         * Do not delete the physical file before the
         * database transaction commits successfully.
         */
        registerCommitDeletion(storageKey);
    }

    private Product requireProduct(long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Product not found"
                        )
                );
    }

    /*
     * Serialize changes to the same product's gallery.
     * This helps protect the primary-image and image-count
     * rules from concurrent requests.
     */
    private Product lockProduct(long productId) {

        Product product = entityManager.find(
                Product.class,
                productId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (product == null) {
            throw new EntityNotFoundException(
                    "Product not found"
            );
        }

        return product;
    }

    private void verifyApprovedSellerOrRoot(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        boolean isRoot = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        Objects.equals(
                                authority.getAuthority(),
                                "ROLE_ROOT"
                        )
                );

        if (isRoot) {
            return;
        }

        boolean isSellerAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        Objects.equals(
                                authority.getAuthority(),
                                "ROLE_SELLER_ADMIN"
                        )
                );

        if (!isSellerAdmin ||
                !(authentication instanceof JwtAuthenticationToken jwt)) {

            throw new AccessDeniedException(
                    "Image management is not permitted"
            );
        }

        Object claim = jwt.getToken().getClaim("userId");

        if (!(claim instanceof Number number)) {
            throw new AccessDeniedException(
                    "Invalid authenticated user"
            );
        }

        var seller = sellerRepository
                .findByUserId(number.longValue())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Seller account not found"
                        )
                );

        if (seller.getApprovalStatus()
                != SellerApprovalStatus.APPROVED) {

            throw new AccessDeniedException(
                    "Seller account is not approved"
            );
        }
    }

    private void registerRollbackDeletion(String storageKey) {

        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            throw new IllegalStateException(
                    "An active transaction is required"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_COMMITTED) {
                            try {
                                storageService.delete(storageKey);
                            } catch (IOException ex) {
                                System.err.println(
                                        "Failed to clean uploaded file: "
                                                + storageKey
                                );
                            }
                        }
                    }
                }
        );
    }

    private void registerCommitDeletion(String storageKey) {

        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            throw new IllegalStateException(
                    "An active transaction is required"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        try {
                            storageService.delete(storageKey);
                        } catch (IOException ex) {
                            System.err.println(
                                    "Failed to remove image file: "
                                            + storageKey
                            );
                        }
                    }
                }
        );
    }

    private ProductImageResponse toResponse(ProductImage image) {

        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getFileSize(),
                image.getSortOrder(),
                image.isPrimary(),
                "/api/products/"
                        + image.getProduct().getId()
                        + "/images/"
                        + image.getId()
                        + "/content"
        );
    }

    public record ProductImageResponse(
            Long id,
            long productId,
            String originalFilename,
            String contentType,
            long fileSize,
            int sortOrder,
            boolean primary,
            String url
    ) {
    }

    public record ImageContent(
            Resource resource,
            String contentType
    ) {
    }
}
