package com.ecommerce.security.service;

import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.LoginResponse;
import com.ecommerce.security.jwt.IssuedToken;
import com.ecommerce.security.jwt.JwtService;
import com.ecommerce.security.model.*;
import com.ecommerce.security.repository.UserAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthenticationServiceTest {


    private UserAccountRepository repository;

    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    private AuthenticationService authenticationService;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        UserAccountRepository.class
                );


        passwordEncoder =
                new BCryptPasswordEncoder();


        jwtService =
                mock(
                        JwtService.class
                );


        authenticationService =
                new AuthenticationService(
                        repository,
                        passwordEncoder,
                        jwtService
                );
    }


    @Test
    void shouldLoginAndReturnJwt() {


        UserAccount user =
                mock(
                        UserAccount.class
                );


        String password =
                "Password123!";


        String passwordHash =
                passwordEncoder.encode(
                        password
                );


        when(
                user.getId()
        ).thenReturn(
                10L
        );


        when(
                user.getEmail()
        ).thenReturn(
                "user@email.com"
        );


        when(
                user.getPasswordHash()
        ).thenReturn(
                passwordHash
        );


        when(
                user.getRole()
        ).thenReturn(
                Role.CUSTOMER
        );


        when(
                user.getStatus()
        ).thenReturn(
                AccountStatus.ACTIVE
        );


        when(
                user.getAuthProvider()
        ).thenReturn(
                AuthProvider.LOCAL
        );


        when(
                repository.findByEmail(
                        "user@email.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );


        Instant issuedAt =
                Instant.parse(
                        "2026-10-31T05:00:00Z"
                );


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


        when(
                jwtService.generateAccessToken(
                        user
                )
        ).thenReturn(

                new IssuedToken(
                        "test.jwt.token",
                        issuedAt,
                        expiresAt
                )
        );


        LoginRequest request =
                new LoginRequest(
                        "USER@EMAIL.COM",
                        password,
                        "test-recaptcha-token"
                );


        LoginResponse response =
                authenticationService.login(
                        request
                );


        assertEquals(
                "test.jwt.token",
                response.accessToken()
        );


        assertEquals(
                "Bearer",
                response.tokenType()
        );


        assertEquals(
                issuedAt,
                response.issuedAt()
        );


        assertEquals(
                expiresAt,
                response.expiresAt()
        );


        assertEquals(
                10L,
                response.userId()
        );


        assertEquals(
                Role.CUSTOMER,
                response.role()
        );


        verify(
                user
        ).recordLogin();


        verify(
                repository
        ).save(
                user
        );


        verify(
                jwtService
        ).generateAccessToken(
                user
        );
    }


    @Test
    void shouldRejectWrongPassword() {


        UserAccount user =
                mock(
                        UserAccount.class
                );


        when(
                user.getStatus()
        ).thenReturn(
                AccountStatus.ACTIVE
        );


        when(
                user.getAuthProvider()
        ).thenReturn(
                AuthProvider.LOCAL
        );


        when(
                user.getPasswordHash()
        ).thenReturn(

                passwordEncoder.encode(
                        "CorrectPassword123!"
                )
        );


        when(
                repository.findByEmail(
                        "user@email.com"
                )
        ).thenReturn(

                Optional.of(
                        user
                )
        );


        LoginRequest request =
                new LoginRequest(
                        "user@email.com",
                        "WrongPassword123!",
                        "test-recaptcha-token"
                );


        assertThrows(

                BadCredentialsException.class,

                () ->
                        authenticationService
                                .login(
                                        request
                                )
        );


        verify(
                jwtService,
                never()
        ).generateAccessToken(
                any()
        );
    }


    @Test
    void shouldRejectInactiveAccount() {


        UserAccount user =
                mock(
                        UserAccount.class
                );


        when(
                user.getStatus()
        ).thenReturn(
                AccountStatus.SUSPENDED
        );


        when(
                repository.findByEmail(
                        "user@email.com"
                )
        ).thenReturn(

                Optional.of(
                        user
                )
        );


        LoginRequest request =
                new LoginRequest(
                        "user@email.com",
                        "Password123!",
                        "test-recaptcha-token"
                );


        assertThrows(

                DisabledException.class,

                () ->
                        authenticationService
                                .login(
                                        request
                                )
        );


        verify(
                jwtService,
                never()
        ).generateAccessToken(
                any()
        );
    }
}