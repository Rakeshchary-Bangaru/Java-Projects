package com.ecommerce.dto;

import com.ecommerce.seller.model.SellerApprovalStatus;


/*
 * =========================================================
 * SELLER PROFILE INFORMATION
 * ---------------------------------------------------------
 * Extra user-facing information available only when the
 * current account is linked to a Seller profile.
 *
 * Internal authorization role is deliberately NOT exposed.
 * =========================================================
 */
public record SellerProfileResponse(

        Long sellerId,

        String businessName,

        SellerApprovalStatus approvalStatus

) {
}