
package com.ecommerce.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductImageHttpSecurityIntegrationTest {

    private static final long PRODUCT_ID = 9999999L;
    private static final long IMAGE_ID = 9999999L;

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile testImage() {

        return new MockMultipartFile(
                "file",
                "security-test.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[] {1, 2, 3}
        );
    }

    /*
     * ========================================================
     * UNAUTHENTICATED USER
     * Expected: HTTP 401
     * ========================================================
     */

    @Test
    void unauthenticatedUserCannotUploadImage()
            throws Exception {

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        PRODUCT_ID
                ).file(testImage())
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotSetPrimaryImage()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/products/{productId}/images/{imageId}/primary",
                        PRODUCT_ID,
                        IMAGE_ID
                )
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotDeleteImage()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        PRODUCT_ID,
                        IMAGE_ID
                )
        ).andExpect(status().isUnauthorized());
    }

    /*
     * ========================================================
     * CUSTOMER
     * Expected: HTTP 403
     * ========================================================
     */

    @Test
    void customerCannotUploadImage()
            throws Exception {

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        PRODUCT_ID
                )
                        .file(testImage())
                        .with(user("test-customer").roles("CUSTOMER"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void customerCannotSetPrimaryImage()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/products/{productId}/images/{imageId}/primary",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .with(user("test-customer").roles("CUSTOMER"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void customerCannotDeleteImage()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .with(user("test-customer").roles("CUSTOMER"))
        ).andExpect(status().isForbidden());
    }

    /*
     * ========================================================
     * IT_SUPPORT
     * Expected: HTTP 403
     * ========================================================
     */

    @Test
    void itSupportCannotUploadImage()
            throws Exception {

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        PRODUCT_ID
                )
                        .file(testImage())
                        .with(user("test-support").roles("IT_SUPPORT"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void itSupportCannotSetPrimaryImage()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/products/{productId}/images/{imageId}/primary",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .with(user("test-support").roles("IT_SUPPORT"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void itSupportCannotDeleteImage()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .with(user("test-support").roles("IT_SUPPORT"))
        ).andExpect(status().isForbidden());
    }
}
