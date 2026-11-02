package com.ecommerce.security.authorization;

import com.ecommerce.repository.SpringDataProductRepository;

import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Component;

@Component("productAuth")
public class ProductOwnershipAuthorization {

    private final SpringDataProductRepository productRepository;

    public ProductOwnershipAuthorization(
            SpringDataProductRepository productRepository
    ) {
        this.productRepository =
                productRepository;
    }


    public boolean isOwner(
            Long productId,
            Authentication authentication
    ) {

        if (productId == null ||
                authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }

        Jwt jwt =
                jwtAuthentication.getToken();

        Number userIdClaim =
                jwt.getClaim("userId");

        if (userIdClaim == null) {
            return false;
        }

        Long userId =
                userIdClaim.longValue();

        return productRepository
                .existsByIdAndSellerUserId(
                        productId,
                        userId
                );
    }
}