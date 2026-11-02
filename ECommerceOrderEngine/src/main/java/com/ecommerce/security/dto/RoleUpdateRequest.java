package com.ecommerce.security.dto;

import com.ecommerce.security.model.Role;

import jakarta.validation.constraints.NotNull;


public record RoleUpdateRequest(

        @NotNull
        Role role

) {
}