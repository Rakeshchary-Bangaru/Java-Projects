package com.ecommerce.security.controller;

import com.ecommerce.security.dto.CurrentUserResponse;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.LoginResponse;

import com.ecommerce.security.model.Role;

import com.ecommerce.security.recaptcha.RecaptchaVerificationService;

import com.ecommerce.security.service.AuthenticationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.web.bind.annotation.*;

import java.util.Objects;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {


    private static final String LOGIN_RECAPTCHA_ACTION =
            "login";


    private final AuthenticationService authenticationService;

    private final RecaptchaVerificationService
            recaptchaVerificationService;


    public AuthenticationController(

            AuthenticationService authenticationService,

            RecaptchaVerificationService
                    recaptchaVerificationService

    ) {

        this.authenticationService =
                authenticationService;

        this.recaptchaVerificationService =
                recaptchaVerificationService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(

            @Valid
            @RequestBody
            LoginRequest request

    ) {


        /*
         * Verify the request came from an interaction that
         * Google reCAPTCHA Enterprise considers legitimate.
         *
         * Authentication is not attempted unless this passes.
         */
        recaptchaVerificationService.verify(

                request.recaptchaToken(),

                LOGIN_RECAPTCHA_ACTION
        );


        LoginResponse response =
                authenticationService.login(
                        request
                );


        return ResponseEntity.ok(
                response
        );
    }


    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> currentUser(

            JwtAuthenticationToken authentication

    ) {


        Jwt jwt =
                authentication.getToken();


        Number userIdClaim =
                Objects.requireNonNull(

                        jwt.getClaim(
                                "userId"
                        ),

                        "JWT userId claim is required"
                );


        String roleClaim =
                Objects.requireNonNull(

                        jwt.getClaimAsString(
                                "role"
                        ),

                        "JWT role claim is required"
                );


        CurrentUserResponse response =
                new CurrentUserResponse(

                        userIdClaim.longValue(),

                        jwt.getSubject(),

                        Role.valueOf(
                                roleClaim
                        ),

                        jwt.getIssuedAt(),

                        jwt.getExpiresAt()
                );


        return ResponseEntity.ok(
                response
        );
    }
}