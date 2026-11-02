package com.ecommerce.security.service;

import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.LoginResponse;
import com.ecommerce.security.jwt.IssuedToken;
import com.ecommerce.security.jwt.JwtService;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new BadCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException(
                    "Account is not active"
            );
        }

        /*
         * Password login is only for LOCAL accounts.
         * Google users will later authenticate through OAuth2.
         */
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        user.recordLogin();

        userAccountRepository.save(user);

        IssuedToken token =
                jwtService.generateAccessToken(user);

        return new LoginResponse(
                token.tokenValue(),
                "Bearer",
                token.issuedAt(),
                token.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}