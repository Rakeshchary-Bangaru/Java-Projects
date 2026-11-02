package com.ecommerce.security.model;

import java.util.EnumSet;
import java.util.Set;

public enum Role {

    /*
     * =========================================================
     * ROOT
     * ---------------------------------------------------------
     * ROOT receives every permission automatically.
     * =========================================================
     */
    ROOT(
            EnumSet.allOf(
                    Permission.class
            )
    ),


    /*
     * =========================================================
     * SELLER ADMIN
     * ---------------------------------------------------------
     * A seller is still a normal shopper.
     *
     * Therefore SELLER_ADMIN receives:
     *
     * - normal customer shopping permissions
     * - seller management permissions
     * =========================================================
     */
    SELLER_ADMIN(
            EnumSet.of(

                    /*
                     * NORMAL SHOPPING
                     */
                    Permission.CHECKOUT_CREATE,
                    Permission.ORDER_READ_OWN,

                    /*
                     * SELLER MANAGEMENT
                     */
                    Permission.PRODUCT_CREATE,
                    Permission.PRODUCT_UPDATE_OWN,
                    Permission.PRODUCT_DELETE_OWN,

                    Permission.INVENTORY_UPDATE_OWN,

                    Permission.ORDER_READ_SELLER,

                    Permission.ANALYTICS_READ_OWN,

                    Permission.AUDIT_READ_OWN
            )
    ),


    /*
     * =========================================================
     * IT SUPPORT
     * ---------------------------------------------------------
     * Support staff can also use the application as normal
     * customers while retaining support capabilities.
     * =========================================================
     */
    IT_SUPPORT(
            EnumSet.of(

                    /*
                     * NORMAL SHOPPING
                     */
                    Permission.CHECKOUT_CREATE,
                    Permission.ORDER_READ_OWN,

                    /*
                     * SUPPORT MANAGEMENT
                     */
                    Permission.AUDIT_READ_GLOBAL,

                    Permission.ACCOUNT_SUSPEND,
                    Permission.ACCOUNT_DELETE_DORMANT,

                    Permission.ROLE_MANAGE
            )
    ),


    /*
     * =========================================================
     * CUSTOMER
     * ---------------------------------------------------------
     * Standard shopping account.
     * =========================================================
     */
    CUSTOMER(
            EnumSet.of(

                    Permission.CHECKOUT_CREATE,

                    Permission.ORDER_READ_OWN
            )
    );


    private final Set<Permission>
            permissions;


    Role(
            Set<Permission> permissions
    ) {

        this.permissions =
                Set.copyOf(
                        permissions
                );
    }


    public Set<Permission> getPermissions() {

        return permissions;
    }
}