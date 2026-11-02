package com.ecommerce.security.controller;

import com.ecommerce.security.dto.AccountStatusUpdateRequest;
import com.ecommerce.security.dto.RoleUpdateRequest;
import com.ecommerce.security.dto.UserAccountResponse;
import com.ecommerce.security.service.UserAccountAdministrationService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/users")
public class UserAccountAdministrationController {

    private final UserAccountAdministrationService administrationService;


    public UserAccountAdministrationController(
            UserAccountAdministrationService administrationService
    ) {

        this.administrationService =
                administrationService;
    }


    @GetMapping
    public List<UserAccountResponse> getUsers() {

        return administrationService
                .findAllUsers();
    }

    @PatchMapping("/{userId}/role")
    public UserAccountResponse changeRole(
            @PathVariable Long userId,
            @Valid
            @RequestBody
            RoleUpdateRequest request
    ) {

        return administrationService
                .changeRole(
                        userId,
                        request.role()
                );
    }

    @PatchMapping("/{userId}/status")
    public UserAccountResponse changeStatus(
            @PathVariable Long userId,
            @Valid
            @RequestBody
            AccountStatusUpdateRequest request
    ) {

        return administrationService.changeStatus(
                userId,
                request.status()
        );
    }

    @PatchMapping("/{userId}/deletion-pending")
    public UserAccountResponse markDeletionPending(
            @PathVariable Long userId
    ) {

        return administrationService
                .markDeletionPending(
                        userId
                );
    }
}