package com.ecommerce.service;

import com.ecommerce.audit.AuditAction;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;
import com.ecommerce.seller.repository.SellerRepository;

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


    public ProductService(
            SpringDataProductRepository productRepository,
            SellerRepository sellerRepository
    ) {

        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }


    /*
     * Create product
     *
     * ROOT:
     * - can create a system/catalogue product
     * - does not need a Seller profile
     *
     * SELLER_ADMIN:
     * - must have PRODUCT_CREATE
     * - must have a Seller profile
     * - Seller must be APPROVED
     * - product is automatically assigned to that Seller
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
         * ROOT can create catalogue/system products
         * without being associated with a Seller.
         */
        if (hasAuthority(
                authentication,
                "ROLE_ROOT"
        )) {

            return productRepository.save(product);
        }


        /*
         * Seller creation requires a JWT because
         * ownership is determined from the signed userId claim.
         */
        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {

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


        long userId =
                userIdClaim.longValue();


        /*
         * Find the Seller associated with the
         * currently authenticated user.
         */
        Seller seller =
                sellerRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Seller profile is required"
                                )
                        );


        /*
         * Having SELLER_ADMIN role is not enough.
         * The Seller must also be approved.
         */
        if (seller.getApprovalStatus()
                != SellerApprovalStatus.APPROVED) {

            throw new AccessDeniedException(
                    "Seller is not approved"
            );
        }


        /*
         * Seller ownership comes from the authenticated
         * account — never from frontend input.
         */
        product.assignSeller(seller);


        return productRepository.save(product);
    }


    /*
     * Find product by id
     */
    @Transactional(readOnly = true)
    public Product findByIdOrThrow(
            long id
    ) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );
    }


    /*
     * Find all products
     */
    @Transactional(readOnly = true)
    public List<Product> findAll() {

        return productRepository.findAll();
    }


    /*
     * Existing generic update method.
     *
     * We will review this separately because the
     * marketplace ownership path currently uses
     * replace() and updatePrice().
     */



    /*
     * Replace product details
     *
     * ROOT:
     * - may update any product
     *
     * SELLER_ADMIN:
     * - needs PRODUCT_UPDATE_OWN
     * - must own the product
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

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found: " + id
                                )
                        );


        /*
         * Modify the existing managed entity instead
         * of constructing a new Product.
         *
         * This preserves:
         * - seller
         * - description
         * - brand
         * - imageUrl
         * - active
         */
        product.replaceDetails(
                name,
                category,
                price
        );


        return product;
    }


    /*
     * Update product price
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

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found: " + id
                                )
                        );


        product.updatePrice(
                newPrice
        );


        return product;
    }


    /*
     * Delete product
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
    public void deleteOrThrow(
            long id
    ) {

        if (!productRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }


        productRepository.deleteById(id);
    }


    /*
     * Utility used internally to check whether
     * Authentication contains a specific authority.
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

    @PreAuthorize("hasRole('SELLER_ADMIN')")
    @Transactional(readOnly = true)
    public List<Product> findAllForSeller(
            Authentication authentication
    ) {

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {

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


        long userId =
                userIdClaim.longValue();


        return productRepository
                .findAllBySellerUserId(
                        userId
                );
    }
}