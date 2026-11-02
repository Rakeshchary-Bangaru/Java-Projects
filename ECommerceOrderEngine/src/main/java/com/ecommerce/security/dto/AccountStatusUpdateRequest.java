package com.ecommerce.security.dto;

import com.ecommerce.security.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusUpdateRequest(

        @NotNull
        AccountStatus status

) {
}