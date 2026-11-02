package com.ecommerce.security.model;



public enum Permission {

    PRODUCT_CREATE,
    PRODUCT_UPDATE_OWN,
    PRODUCT_DELETE_OWN,

    INVENTORY_UPDATE_OWN,

    /*
     * Normal shopping capabilities.
     *
     * These are available to every authenticated account,
     * regardless of whether the account also has seller,
     * support, or ROOT privileges.
     */
    CHECKOUT_CREATE,
    ORDER_READ_OWN,

    /*
     * Seller-specific order access.
     */
    ORDER_READ_SELLER,

    ANALYTICS_READ_OWN,
    ANALYTICS_READ_GLOBAL,

    AUDIT_READ_OWN,
    AUDIT_READ_GLOBAL,

    ACCOUNT_SUSPEND,
    ACCOUNT_DELETE_DORMANT,

    ROLE_MANAGE,

    SYSTEM_ADMIN
}