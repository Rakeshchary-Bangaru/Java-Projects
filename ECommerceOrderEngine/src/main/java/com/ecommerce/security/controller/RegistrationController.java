package com.ecommerce.security.controller;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @Valid @RequestBody RegisterRequest request
    ) {

        UserAccount user =
                registrationService.registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "id", user.getId(),
                                "email", user.getEmail(),
                                "role", user.getRole(),
                                "status", user.getStatus()
                        )
                );
    }
}