
package com.ecommerce.service;

import com.ecommerce.audit.AuditAction;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;
import com.ecommerce.seller.repository.SellerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final SpringDataProductRepository productRepository;

    private final SellerRepository sellerRepository;

    private final ProductImageDeletionService imageDeletionService;

    /*
     * Spring uses this constructor in the application.
     * Image cleanup is a required dependency.
     */
    @Autowired
    public ProductService(
            SpringDataProductRepository productRepository,
            SellerRepository sellerRepository,
            ProductImageDeletionService imageDeletionService
    ) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.imageDeletionService = imageDeletionService;
    }

    /*
     * Backward-compatible constructor for existing
     * unit tests that manually instantiate ProductService.
     *
     * The Spring application uses the three-argument
     * constructor above.
     */
    public ProductService(
            SpringDataProductRepository productRepository,
            SellerRepository sellerRepository
    ) {
        this(productRepository, sellerRepository, null);
    }

    /*
     * =========================================================
     * CREATE PRODUCT
     * =========================================================
     */
    @AuditAction(
            action = "PRODUCT_CREATED",
            entity = "PRODUCT"
    )
    @PreAuthorize(
            "hasRole('ROOT') || hasAuthority('PRODUCT_CREATE')"
    )
    @Transactional
    public Product save(
            Product product,
            Authentication authentication
    ) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        /*
         * ROOT creates catalogue products without
         * requiring a Seller profile.
         */
        if (hasAuthority(authentication, "ROLE_ROOT")) {

            return productRepository.save(product);
        }

        /*
         * Seller creation requires JWT authentication.
         */
        if (!(authentication instanceof
                JwtAuthenticationToken jwtAuthentication)) {

            throw new AccessDeniedException(
                    "JWT authentication is required"
            );
        }

        Number userIdClaim =
                jwtAuthentication
                        .getToken()
                        .getClaim("userId");

        if (userIdClaim == null) {
            throw new AccessDeniedException(
                    "JWT userId claim is required"
            );
        }

        long userId = userIdClaim.longValue();

        Seller seller = sellerRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Seller profile is required"
                        )
                );

        if (seller.getApprovalStatus()
                != SellerApprovalStatus.APPROVED) {

            throw new AccessDeniedException(
                    "Seller is not approved"
            );
        }

        /*
         * Ownership comes from the authenticated account.
         */
        product.assignSeller(seller);

        return productRepository.save(product);
    }

    /*
     * =========================================================
     * FIND PRODUCT BY ID
     * =========================================================
     */
    @Transactional(readOnly = true)
    public Product findByIdOrThrow(long id) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );
    }

    /*
     * =========================================================
     * FIND ALL PRODUCTS
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<Product> findAll() {

        return productRepository.findAll();
    }

    /*
     * =========================================================
     * REPLACE PRODUCT DETAILS
     * =========================================================
     */
    @AuditAction(
            action = "PRODUCT_REPLACED",
            entity = "PRODUCT"
    )
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('PRODUCT_UPDATE_OWN') && " +
                    "@productAuth.isOwner(#id, authentication))"
    )
    @Transactional
    public Product replace(
            long id,
            String name,
            String category,
            BigDecimal price
    ) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );

        /*
         * Preserve seller ownership and additional
         * product attributes during updates.
         */
        product.replaceDetails(
                name,
                category,
                price
        );

        return product;
    }

    /*
     * =========================================================
     * UPDATE PRODUCT PRICE
     * =========================================================
     */
    @AuditAction(
            action = "PRODUCT_PRICE_UPDATED",
            entity = "PRODUCT"
    )
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('PRODUCT_UPDATE_OWN') && " +
                    "@productAuth.isOwner(#id, authentication))"
    )
    @Transactional
    public Product updatePrice(
            long id,
            BigDecimal newPrice
    ) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );

        product.updatePrice(newPrice);

        return product;
    }

    /*
     * =========================================================
     * DELETE PRODUCT
     *
     * ROOT:
     * - Can delete any product.
     *
     * SELLER_ADMIN:
     * - Requires PRODUCT_DELETE_OWN.
     * - Must own the product.
     *
     * Image metadata is removed transactionally.
     * Physical files are removed only after commit.
     * =========================================================
     */

    @AuditAction(
            action = "PRODUCT_DELETED",
            entity = "PRODUCT"
    )
    @PreAuthorize(
            "hasRole('ROOT') || " +
                    "(hasAuthority('PRODUCT_DELETE_OWN') && " +
                    "@productAuth.isOwner(#id, authentication))"
    )
    @Transactional
    public void deleteOrThrow(long id) {

        /*
         * Preserve the existing repository interaction
         * expected by the original unit tests.
         */
        if (!productRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }

        /*
         * Remove image metadata within the transaction.
         * Physical files are deleted only after commit.
         */
        if (imageDeletionService != null) {

            imageDeletionService.deleteImagesForProduct(id);
        }

        /*
         * Preserve the original deleteById operation.
         */
        productRepository.deleteById(id);

        /*
         * Flush database changes so FK violations
         * are detected before the transaction commits.
         */
        productRepository.flush();
    }


    /*
     * =========================================================
     * AUTHORITY UTILITY
     * =========================================================
     */
    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        authority.equals(
                                grantedAuthority.getAuthority()
                        )
                );
    }

    /*
     * =========================================================
     * SELLER PRODUCT LISTING
     * =========================================================
     */
    @PreAuthorize("hasRole('SELLER_ADMIN')")
    @Transactional(readOnly = true)
    public List<Product> findAllForSeller(
            Authentication authentication
    ) {

        if (!(authentication instanceof
                JwtAuthenticationToken jwtAuthentication)) {

            throw new AccessDeniedException(
                    "JWT authentication is required"
            );
        }

        Number userIdClaim =
                jwtAuthentication
                        .getToken()
                        .getClaim("userId");

        if (userIdClaim == null) {
            throw new AccessDeniedException(
                    "JWT userId claim is required"
            );
        }

        long userId = userIdClaim.longValue();

        return productRepository
                .findAllBySellerUserId(userId);
    }
}
