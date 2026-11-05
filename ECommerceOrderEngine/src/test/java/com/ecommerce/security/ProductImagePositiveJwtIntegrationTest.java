
package com.ecommerce.security;

import com.ecommerce.model.Product;
import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.repository.SpringDataProductImageRepository;

import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.repository.SellerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductImagePositiveJwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataProductImageRepository imageRepository;

    private UserAccount createUser(Role role) {

        UserAccount user = new UserAccount(
                "v11-" + UUID.randomUUID() + "@test.com",
                "test-only-password-hash",
                role,
                AuthProvider.LOCAL
        );

        return userAccountRepository.saveAndFlush(user);
    }

    private Seller createSeller(
            UserAccount account,
            UserAccount root,
            boolean approved
    ) {

        Seller seller = new Seller(
                account,
                "V11 Test Business"
        );

        if (approved) {
            seller.approve(root.getId());
        }

        return sellerRepository.saveAndFlush(seller);
    }

    private Product createProduct(Seller seller) {

        Product product = new Product(
                "V11 Test Product",
                "Testing",
                new BigDecimal("99.99"),
                "JWT image authorization test",
                "V11"
        );

        if (seller != null) {
            product.assignSeller(seller);
        }

        return productRepository.saveAndFlush(product);
    }

    private ProductImage createImage(
            Product product,
            boolean primary,
            int sortOrder
    ) {

        ProductImage image = new ProductImage(
                product,
                UUID.randomUUID() + ".png",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                68L,
                sortOrder,
                primary
        );

        return imageRepository.saveAndFlush(image);
    }

    private String createToken(UserAccount user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://ecommerce-order-engine")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("authVersion", user.getAuthVersion())
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }


    private MockMultipartFile testImage() throws IOException {

        BufferedImage image = new BufferedImage(
                10,
                10,
                BufferedImage.TYPE_INT_RGB
        );

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        boolean written = ImageIO.write(
                image,
                "png",
                output
        );

        if (!written) {
            throw new IOException(
                    "PNG ImageIO writer is unavailable"
            );
        }

        return new MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                output.toByteArray()
        );
    }


    /*
     * ROOT - POSITIVE AUTHORIZATION
     */

    @Test
    void rootCanUploadImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        Product product = createProduct(null);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        product.getId()
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(root)
                        )
        ).andExpect(status().isCreated());
    }

    @Test
    void rootCanSetPrimaryImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        Product product = createProduct(null);

        createImage(product, true, 0);
        ProductImage second = createImage(product, false, 1);

        mockMvc.perform(
                        put(
                                "/api/products/{productId}/images/{imageId}/primary",
                                product.getId(),
                                second.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + createToken(root)
                                )
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true));
    }

    @Test
    void rootCanDeleteImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        Product product = createProduct(null);
        ProductImage image = createImage(product, true, 0);

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        product.getId(),
                        image.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(root)
                        )
        ).andExpect(status().isNoContent());
    }

    /*
     * APPROVED SELLER - POSITIVE AUTHORIZATION
     */

    @Test
    void approvedSellerCanUploadOwnImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        UserAccount sellerUser = createUser(Role.SELLER_ADMIN);

        Seller seller = createSeller(sellerUser, root, true);
        Product product = createProduct(seller);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        product.getId()
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(sellerUser)
                        )
        ).andExpect(status().isCreated());
    }

    @Test
    void approvedSellerCanSetOwnPrimaryImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        UserAccount sellerUser = createUser(Role.SELLER_ADMIN);

        Seller seller = createSeller(sellerUser, root, true);
        Product product = createProduct(seller);

        createImage(product, true, 0);
        ProductImage second = createImage(product, false, 1);

        mockMvc.perform(
                        put(
                                "/api/products/{productId}/images/{imageId}/primary",
                                product.getId(),
                                second.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + createToken(sellerUser)
                                )
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true));
    }

    @Test
    void approvedSellerCanDeleteOwnImage() throws Exception {

        UserAccount root = createUser(Role.ROOT);
        UserAccount sellerUser = createUser(Role.SELLER_ADMIN);

        Seller seller = createSeller(sellerUser, root, true);
        Product product = createProduct(seller);
        ProductImage image = createImage(product, true, 0);

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        product.getId(),
                        image.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(sellerUser)
                        )
        ).andExpect(status().isNoContent());
    }

    /*
     * CROSS-SELLER AUTHORIZATION
     */

    @Test
    void sellerCannotUploadToAnotherSellersProduct()
            throws Exception {

        UserAccount root = createUser(Role.ROOT);

        UserAccount firstUser = createUser(Role.SELLER_ADMIN);
        UserAccount secondUser = createUser(Role.SELLER_ADMIN);

        createSeller(firstUser, root, true);
        Seller secondSeller = createSeller(secondUser, root, true);

        Product secondProduct = createProduct(secondSeller);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        secondProduct.getId()
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(firstUser)
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotDeleteAnotherSellersImage()
            throws Exception {

        UserAccount root = createUser(Role.ROOT);

        UserAccount firstUser = createUser(Role.SELLER_ADMIN);
        UserAccount secondUser = createUser(Role.SELLER_ADMIN);

        createSeller(firstUser, root, true);
        Seller secondSeller = createSeller(secondUser, root, true);

        Product secondProduct = createProduct(secondSeller);
        ProductImage image = createImage(secondProduct, true, 0);

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        secondProduct.getId(),
                        image.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(firstUser)
                        )
        ).andExpect(status().isForbidden());
    }

    /*
     * PENDING SELLER
     */

    @Test
    void pendingSellerCannotUploadOwnImage()
            throws Exception {

        UserAccount root = createUser(Role.ROOT);
        UserAccount sellerUser = createUser(Role.SELLER_ADMIN);

        Seller pendingSeller =
                createSeller(sellerUser, root, false);

        Product product = createProduct(pendingSeller);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        product.getId()
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(sellerUser)
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotSetAnotherSellersPrimaryImage()
            throws Exception {

        UserAccount root = createUser(Role.ROOT);

        UserAccount firstUser =
                createUser(Role.SELLER_ADMIN);

        UserAccount secondUser =
                createUser(Role.SELLER_ADMIN);

        createSeller(firstUser, root, true);

        Seller secondSeller =
                createSeller(secondUser, root, true);

        Product product = createProduct(secondSeller);

        createImage(product, true, 0);

        ProductImage secondImage =
                createImage(product, false, 1);

        mockMvc.perform(
                put(
                        "/api/products/{productId}/images/{imageId}/primary",
                        product.getId(),
                        secondImage.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(firstUser)
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void rejectedSellerCannotUploadOwnImage()
            throws Exception {

        UserAccount root = createUser(Role.ROOT);

        UserAccount sellerUser =
                createUser(Role.SELLER_ADMIN);

        Seller seller =
                createSeller(sellerUser, root, false);

        seller.reject();

        sellerRepository.saveAndFlush(seller);

        Product product = createProduct(seller);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        product.getId()
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + createToken(sellerUser)
                        )
        ).andExpect(status().isForbidden());
    }
}
