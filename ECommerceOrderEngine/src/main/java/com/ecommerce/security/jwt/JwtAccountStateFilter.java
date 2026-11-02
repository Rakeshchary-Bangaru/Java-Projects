package com.ecommerce.security.jwt;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtAccountStateFilter
        extends OncePerRequestFilter {


    private final UserAccountRepository
            userAccountRepository;


    public JwtAccountStateFilter(
            UserAccountRepository userAccountRepository
    ) {

        this.userAccountRepository =
                userAccountRepository;
    }


    /*
     * =========================================================
     * VALIDATE CURRENT ACCOUNT STATE
     * ---------------------------------------------------------
     * JWT signature/expiry has already been validated by
     * Spring Security before this filter runs.
     *
     * This filter additionally checks:
     *
     * - user still exists
     * - authVersion still matches
     * - JWT role still matches database role
     * - account is still ACTIVE
     *
     * Therefore role/status changes invalidate old tokens.
     * =========================================================
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        /*
         * Public/anonymous requests simply continue.
         */
        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        Jwt jwt =
                jwtAuthentication
                        .getToken();


        Number userIdClaim =
                jwt.getClaim(
                        "userId"
                );


        Number authVersionClaim =
                jwt.getClaim(
                        "authVersion"
                );


        String roleClaim =
                jwt.getClaimAsString(
                        "role"
                );


        /*
         * Tokens issued before authVersion was introduced
         * are deliberately rejected.
         *
         * Users simply sign in again and receive a V7 token
         * containing the new claim.
         */
        if (userIdClaim == null ||
                authVersionClaim == null ||
                roleClaim == null ||
                roleClaim.isBlank()) {

            rejectAuthentication(
                    response
            );

            return;
        }


        long userId =
                userIdClaim.longValue();


        UserAccount user =
                userAccountRepository
                        .findById(
                                userId
                        )
                        .orElse(null);


        /*
         * Account no longer exists.
         */
        if (user == null) {

            rejectAuthentication(
                    response
            );

            return;
        }


        /*
         * =====================================================
         * AUTH VERSION
         * -----------------------------------------------------
         * This is the main session invalidation check.
         * =====================================================
         */
        if (user.getAuthVersion()
                != authVersionClaim.longValue()) {

            rejectAuthentication(
                    response
            );

            return;
        }


        /*
         * =====================================================
         * ROLE CONSISTENCY
         * -----------------------------------------------------
         * Defense in depth.
         *
         * Even if version management were accidentally missed,
         * an old role claim cannot continue authorizing access.
         * =====================================================
         */
        if (!user.getRole()
                .name()
                .equals(roleClaim)) {

            rejectAuthentication(
                    response
            );

            return;
        }


        /*
         * =====================================================
         * ACCOUNT STATUS
         * =====================================================
         */
        if (user.getStatus()
                != AccountStatus.ACTIVE) {

            rejectAuthentication(
                    response
            );

            return;
        }


        filterChain.doFilter(
                request,
                response
        );
    }


    /*
     * =========================================================
     * REJECT OLD / INVALID SESSION
     * =========================================================
     */
    private void rejectAuthentication(
            HttpServletResponse response
    ) throws IOException {


        SecurityContextHolder
                .clearContext();


        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );


        response.setContentType(
                "application/json"
        );


        response
                .getWriter()
                .write(
                        """
                        {
                          "error": "Authentication is no longer valid. Please sign in again."
                        }
                        """
                );
    }
}