package com.ecommerce.security.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionMappingTest {

    @Test
    void itSupportHasRoleManagementPermission() {

        assertTrue(
                Role.IT_SUPPORT
                        .getPermissions()
                        .contains(
                                Permission.ROLE_MANAGE
                        )
        );
    }
}