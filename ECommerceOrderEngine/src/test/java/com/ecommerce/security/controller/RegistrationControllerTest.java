package com.ecommerce.security.controller;

import com.ecommerce.config.MvcControllerTest;
import com.ecommerce.security.config.SecurityConfig;
import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.service.RegistrationService;
import com.ecommerce.security.recaptcha.RecaptchaVerificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MvcControllerTest(
        controllers = RegistrationController.class
)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private RecaptchaVerificationService recaptchaVerificationService;

    @Test
    void shouldRegisterCustomer() throws Exception {

        UserAccount user = mock(UserAccount.class);

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("user@email.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        when(user.getStatus()).thenReturn(AccountStatus.ACTIVE);

        when(
                registrationService.registerCustomer(
                        any(RegisterRequest.class)
                )
        ).thenReturn(user);

        RegisterRequest request =
                new RegisterRequest(
                        "Rakesh Bangaru",
                        "test@email.com",
                        "Password123!",
                        "test-recaptcha-token"

                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.email")
                                .value("user@email.com")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("CUSTOMER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );
    }

    @Test
    void shouldVerifyRecaptchaBeforeRegisteringCustomer() throws Exception {

        UserAccount user = mock(UserAccount.class);

        when(user.getId())
                .thenReturn(1L);

        when(user.getEmail())
                .thenReturn("test@example.com");

        when(user.getRole())
                .thenReturn(Role.CUSTOMER);

        when(user.getStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(registrationService.registerCustomer(any(RegisterRequest.class)))
                .thenReturn(user);


        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "Test User",
                                      "email": "test@example.com",
                                      "password": "Password123!",
                                      "recaptchaToken": "test-recaptcha-token"
                                    }
                                    """)
                )
                .andExpect(status().isCreated());


        verify(recaptchaVerificationService)
                .verify(
                        "test-recaptcha-token",
                        "register"
                );

        verify(registrationService)
                .registerCustomer(
                        any(RegisterRequest.class)
                );
    }

    @Test
    void shouldRejectRegistrationWhenRecaptchaVerificationFails()
            throws Exception {

        doThrow(
                new IllegalArgumentException(
                        "reCAPTCHA verification failed"
                )
        )
                .when(recaptchaVerificationService)
                .verify(
                        "invalid-token",
                        "register"
                );


        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "name": "Test User",
                                      "email": "test@example.com",
                                      "password": "Password123!",
                                      "recaptchaToken": "invalid-token"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());


        verify(recaptchaVerificationService)
                .verify(
                        "invalid-token",
                        "register"
                );

        verify(
                registrationService,
                never()
        )
                .registerCustomer(
                        any(RegisterRequest.class)
                );
    }
}