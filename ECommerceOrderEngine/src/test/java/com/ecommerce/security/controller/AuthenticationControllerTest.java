package com.ecommerce.security.controller;

import com.ecommerce.config.MvcControllerTest;
import com.ecommerce.controller.CustomerOrderController;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.LoginResponse;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.recaptcha.RecaptchaVerificationService;
import com.ecommerce.security.service.AuthenticationService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MvcControllerTest(
        controllers = AuthenticationController.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JsonMapper jsonMapper;


    @MockitoBean
    private AuthenticationService authenticationService;


    @MockitoBean
    private RecaptchaVerificationService
            recaptchaVerificationService;


    @Test
    void shouldLoginAndReturnJwt() throws Exception {


        Instant issuedAt =
                Instant.parse(
                        "2026-10-31T05:30:00Z"
                );


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


        LoginResponse response =
                new LoginResponse(

                        "test.jwt.token",

                        "Bearer",

                        issuedAt,

                        expiresAt,

                        10L,

                        "user@email.com",

                        Role.CUSTOMER
                );


        when(
                authenticationService.login(
                        any(LoginRequest.class)
                )
        ).thenReturn(
                response
        );


        LoginRequest request =
                new LoginRequest(

                        "user@email.com",

                        "Password123!",

                        "test-recaptcha-token"
                );


        mockMvc.perform(

                        post(
                                "/api/auth/login"
                        )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        jsonMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.accessToken"
                        )
                                .value(
                                        "test.jwt.token"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.tokenType"
                        )
                                .value(
                                        "Bearer"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.userId"
                        )
                                .value(
                                        10
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.email"
                        )
                                .value(
                                        "user@email.com"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.role"
                        )
                                .value(
                                        "CUSTOMER"
                                )
                );


        verify(
                recaptchaVerificationService
        ).verify(
                "test-recaptcha-token",
                "login"
        );


        verify(
                authenticationService
        ).login(
                any(LoginRequest.class)
        );
    }


    @Test
    void shouldReturnCurrentAuthenticatedUser()
            throws Exception {


        Instant issuedAt =
                Instant.parse(
                        "2026-10-31T05:30:00Z"
                );


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


        Jwt jwt =
                Jwt.withTokenValue(
                                "test.jwt.token"
                        )

                        .header(
                                "alg",
                                "HS256"
                        )

                        .subject(
                                "user@email.com"
                        )

                        .issuedAt(
                                issuedAt
                        )

                        .expiresAt(
                                expiresAt
                        )

                        .claim(
                                "userId",
                                10L
                        )

                        .claim(
                                "role",
                                "CUSTOMER"
                        )

                        .build();


        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(

                        jwt,

                        List.of(

                                new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"
                                )
                        )
                );


        mockMvc.perform(

                        get(
                                "/api/auth/me"
                        )

                                .principal(
                                        authentication
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.userId"
                        )
                                .value(
                                        10
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.email"
                        )
                                .value(
                                        "user@email.com"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.role"
                        )
                                .value(
                                        "CUSTOMER"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.issuedAt"
                        )
                                .exists()
                )

                .andExpect(
                        jsonPath(
                                "$.expiresAt"
                        )
                                .exists()
                );
    }


    @Test
    void shouldRejectLoginWhenRecaptchaVerificationFails()
            throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "user@email.com",
                        "Password123!",
                        "bad-recaptcha-token"
                );


        doThrow(
                new IllegalArgumentException(
                        "reCAPTCHA verification failed"
                )
        )
                .when(
                        recaptchaVerificationService
                )
                .verify(
                        "bad-recaptcha-token",
                        "login"
                );


        mockMvc.perform(

                        post(
                                "/api/auth/login"
                        )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        jsonMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )

                .andExpect(
                        status().isBadRequest()
                );


        verify(
                authenticationService,
                never()
        ).login(
                any(LoginRequest.class)
        );
    }

}