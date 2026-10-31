package com.ecommerce.security.service;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private UserAccountRepository repository;
    private PasswordEncoder passwordEncoder;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {

        repository =
                mock(UserAccountRepository.class);

        passwordEncoder =
                new BCryptPasswordEncoder();

        registrationService =
                new RegistrationService(
                        repository,
                        passwordEncoder
                );
    }


    @Test
    void shouldRegisterCustomerWithHashedPassword() {

        RegisterRequest request =
                new RegisterRequest(
                        "TEST@EMAIL.COM",
                        "Password123!"
                );

        when(repository.existsByEmail(
                "test@email.com"
        )).thenReturn(false);

        when(repository.save(any(UserAccount.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserAccount result =
                registrationService
                        .registerCustomer(request);

        assertEquals(
                "test@email.com",
                result.getEmail()
        );

        assertEquals(
                Role.CUSTOMER,
                result.getRole()
        );

        assertEquals(
                AuthProvider.LOCAL,
                result.getAuthProvider()
        );

        assertNotEquals(
                "Password123!",
                result.getPasswordHash()
        );

        assertTrue(
                passwordEncoder.matches(
                        "Password123!",
                        result.getPasswordHash()
                )
        );
    }


    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest(
                        "user@email.com",
                        "Password123!"
                );

        when(repository.existsByEmail(
                "user@email.com"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        registrationService
                                .registerCustomer(request)
        );

        verify(
                repository,
                never()
        ).save(any());
    }
}