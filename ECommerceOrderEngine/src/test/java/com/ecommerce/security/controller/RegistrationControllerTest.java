package com.ecommerce.security.controller;

import com.ecommerce.security.config.SecurityConfig;
import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.service.RegistrationService;

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

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private RegistrationService registrationService;

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
                        "user@email.com",
                        "Password123!"
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
}