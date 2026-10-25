package com.ecommerce.audit;

import java.time.LocalDateTime;


public record AuditRecord(

        LocalDateTime timestamp,

        String actor,

        String action,

        String entity,

        String entityId,

        String method,

        String details,

        String status
) {
}