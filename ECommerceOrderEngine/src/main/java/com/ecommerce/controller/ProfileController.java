package com.ecommerce.controller;

import com.ecommerce.dto.ProfileResponse;

import com.ecommerce.service.ProfileService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*
 * =========================================================
 * CURRENT USER PROFILE
 * =========================================================
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService
            profileService;


    public ProfileController(
            ProfileService profileService
    ) {

        this.profileService =
                profileService;
    }


    /*
     * =========================================================
     * GET /api/profile/me
     * ---------------------------------------------------------
     * Returns only the currently authenticated user's profile.
     *
     * There is deliberately no endpoint such as:
     *
     * /api/profile/{userId}
     *
     * for ordinary users.
     * =========================================================
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getMyProfile(
            Authentication authentication
    ) {

        long userAccountId =
                extractUserAccountId(
                        authentication
                );


        ProfileResponse profile =
                profileService
                        .getProfile(
                                userAccountId
                        );


        return ResponseEntity.ok(
                profile
        );
    }


    /*
     * =========================================================
     * JWT USER ACCOUNT ID
     * =========================================================
     */
    private long extractUserAccountId(
            Authentication authentication
    ) {

        if (authentication == null) {

            throw new IllegalStateException(
                    "Authentication is required"
            );
        }


        if (!(authentication.getPrincipal()
                instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated principal is not a JWT"
            );
        }


        Number userIdClaim =
                jwt.getClaim(
                        "userId"
                );


        if (userIdClaim == null) {

            throw new IllegalStateException(
                    "Authenticated token does not contain userId"
            );
        }


        return userIdClaim.longValue();
    }
}