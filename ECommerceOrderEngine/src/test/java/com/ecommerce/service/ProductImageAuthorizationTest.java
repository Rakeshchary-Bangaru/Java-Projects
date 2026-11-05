
package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.storage.ProductImageStorageService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(
        ProductImageAuthorizationTest.TestConfiguration.class
)
class ProductImageAuthorizationTest {

    private static final long PRODUCT_ID = 8802L;
    private static final long IMAGE_ID = 1L;
    private static final long SELLER_USER_ID = 42L;

    @Autowired
    private ProductImageService imageService;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataProductImageRepository imageRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestOwnership ownership;

    @BeforeEach
    void setUp() {
        reset(
                productRepository,
                imageRepository,
                sellerRepository,
                entityManager,
                ownership
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rootCanSetPrimaryOnAnyProduct() {

        Authentication root = jwtAuthentication(
                "ROLE_ROOT"
        );

        authenticate(root);
        mockExistingImage();

        assertDoesNotThrow(() ->
                imageService.setPrimary(
                        PRODUCT_ID,
                        IMAGE_ID,
                        root
                )
        );

        verify(ownership, never())
                .isOwner(anyLong(), any());
    }

    @Test
    void approvedSellerCanSetPrimaryOnOwnedProduct() {

        Authentication seller = jwtAuthentication(
                "ROLE_SELLER_ADMIN"
        );

        authenticate(seller);

        when(ownership.isOwner(
                eq(PRODUCT_ID),
                any(Authentication.class)
        )).thenReturn(true);

        mockApprovedSeller();
        mockExistingImage();

        assertDoesNotThrow(() ->
                imageService.setPrimary(
                        PRODUCT_ID,
                        IMAGE_ID,
                        seller
                )
        );
    }

    @Test
    void sellerCannotModifyAnotherSellersImages() {

        Authentication seller = jwtAuthentication(
                "ROLE_SELLER_ADMIN"
        );

        authenticate(seller);

        when(ownership.isOwner(
                eq(PRODUCT_ID),
                any(Authentication.class)
        )).thenReturn(false);

        assertAll(
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.upload(
                                PRODUCT_ID,
                                mock(MultipartFile.class),
                                seller
                        )
                ),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.setPrimary(
                                PRODUCT_ID,
                                IMAGE_ID,
                                seller
                        )
                ),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.delete(
                                PRODUCT_ID,
                                IMAGE_ID,
                                seller
                        )
                )
        );

        verifyNoInteractions(entityManager);
    }

    @Test
    void unapprovedSellerCannotSetPrimaryEvenWhenOwner() {

        Authentication sellerAuth = jwtAuthentication(
                "ROLE_SELLER_ADMIN"
        );

        authenticate(sellerAuth);

        when(ownership.isOwner(
                eq(PRODUCT_ID),
                any(Authentication.class)
        )).thenReturn(true);

        Seller seller = mock(Seller.class);

        when(sellerRepository.findByUserId(SELLER_USER_ID))
                .thenReturn(Optional.of(seller));

        when(seller.getApprovalStatus())
                .thenReturn(SellerApprovalStatus.PENDING);

        assertThrows(
                AccessDeniedException.class,
                () -> imageService.setPrimary(
                        PRODUCT_ID,
                        IMAGE_ID,
                        sellerAuth
                )
        );

        verifyNoInteractions(entityManager);
    }

    @Test
    void customerCannotModifyImages() {

        Authentication customer = simpleAuthentication(
                "ROLE_CUSTOMER"
        );

        authenticate(customer);
        assertAllMutationsDenied(customer);
    }

    @Test
    void itSupportCannotModifyImages() {

        Authentication support = simpleAuthentication(
                "ROLE_IT_SUPPORT"
        );

        authenticate(support);
        assertAllMutationsDenied(support);
    }

    @Test
    void sellerWithoutProfileCannotModifyImages() {

        Authentication seller = jwtAuthentication(
                "ROLE_SELLER_ADMIN"
        );

        authenticate(seller);

        when(ownership.isOwner(
                eq(PRODUCT_ID),
                any(Authentication.class)
        )).thenReturn(true);

        when(sellerRepository.findByUserId(SELLER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> imageService.setPrimary(
                        PRODUCT_ID,
                        IMAGE_ID,
                        seller
                )
        );

        verifyNoInteractions(entityManager);
    }

    private void assertAllMutationsDenied(
            Authentication authentication
    ) {

        assertAll(
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.upload(
                                PRODUCT_ID,
                                mock(MultipartFile.class),
                                authentication
                        )
                ),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.setPrimary(
                                PRODUCT_ID,
                                IMAGE_ID,
                                authentication
                        )
                ),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> imageService.delete(
                                PRODUCT_ID,
                                IMAGE_ID,
                                authentication
                        )
                )
        );

        verifyNoInteractions(entityManager);
    }

    private void mockApprovedSeller() {

        Seller seller = mock(Seller.class);

        when(sellerRepository.findByUserId(SELLER_USER_ID))
                .thenReturn(Optional.of(seller));

        when(seller.getApprovalStatus())
                .thenReturn(SellerApprovalStatus.APPROVED);
    }

    private void mockExistingImage() {

        Product product = mock(Product.class);
        ProductImage image = mock(ProductImage.class);

        when(product.getId())
                .thenReturn(PRODUCT_ID);

        when(image.getId())
                .thenReturn(IMAGE_ID);

        when(image.getProduct())
                .thenReturn(product);

        when(entityManager.find(
                Product.class,
                PRODUCT_ID,
                LockModeType.PESSIMISTIC_WRITE
        )).thenReturn(product);

        when(imageRepository.findByIdAndProduct_Id(
                IMAGE_ID,
                PRODUCT_ID
        )).thenReturn(Optional.of(image));

        when(imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(PRODUCT_ID))
                .thenReturn(List.of(image));
    }

    private Authentication jwtAuthentication(String role) {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("userId", SELLER_USER_ID)
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    private Authentication simpleAuthentication(String role) {

        return new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    private void authenticate(Authentication authentication) {

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    public static class TestOwnership {

        public boolean isOwner(
                long productId,
                Authentication authentication
        ) {
            return false;
        }
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        SpringDataProductRepository productRepository() {
            return mock(SpringDataProductRepository.class);
        }

        @Bean
        SpringDataProductImageRepository imageRepository() {
            return mock(SpringDataProductImageRepository.class);
        }

        @Bean
        SellerRepository sellerRepository() {
            return mock(SellerRepository.class);
        }

        @Bean
        ProductImageStorageService storageService() {
            return mock(ProductImageStorageService.class);
        }

        @Bean
        EntityManager entityManager() {
            return mock(EntityManager.class);
        }

        @Bean("productAuth")
        TestOwnership productAuth() {
            return mock(TestOwnership.class);
        }

        @Bean
        ProductImageService productImageService(
                SpringDataProductRepository products,
                SpringDataProductImageRepository images,
                SellerRepository sellers,
                ProductImageStorageService storage,
                EntityManager entityManager
        ) {
            return new ProductImageService(
                    products,
                    images,
                    sellers,
                    storage,
                    entityManager
            );
        }
    }
}
