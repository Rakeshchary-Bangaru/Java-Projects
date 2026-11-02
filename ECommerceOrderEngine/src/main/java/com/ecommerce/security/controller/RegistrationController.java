package com.ecommerce.security.controller;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.recaptcha.RecaptchaVerificationService;
import com.ecommerce.security.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class RegistrationController {


    private static final String REGISTER_RECAPTCHA_ACTION =
            "register";


    private final RegistrationService registrationService;

    private final RecaptchaVerificationService
            recaptchaVerificationService;


    public RegistrationController(

            RegistrationService registrationService,

            RecaptchaVerificationService
                    recaptchaVerificationService

    ) {

        this.registrationService =
                registrationService;

        this.recaptchaVerificationService =
                recaptchaVerificationService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(

            @Valid
            @RequestBody
            RegisterRequest request

    ) {


        /*
         * Verify the public registration request before
         * creating any account or customer data.
         */
        recaptchaVerificationService.verify(

                request.recaptchaToken(),

                REGISTER_RECAPTCHA_ACTION
        );


        UserAccount user =
                registrationService
                        .registerCustomer(
                                request
                        );


        return ResponseEntity

                .status(
                        HttpStatus.CREATED
                )

                .body(
                        Map.of(

                                "id",
                                user.getId(),

                                "email",
                                user.getEmail(),

                                "role",
                                user.getRole(),

                                "status",
                                user.getStatus()
                        )
                );
    }
}