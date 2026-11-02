
package com.ecommerce.security.handler;

import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.service.GoogleLoginCodeService;
import com.ecommerce.security.service.GoogleOAuth2AccountService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleOAuth2AccountService accountService;
    private final GoogleLoginCodeService loginCodeService;
    private final String frontendUrl;

    private static final Logger log =
            LoggerFactory.getLogger(
                    GoogleOAuth2SuccessHandler.class
            );

    public GoogleOAuth2SuccessHandler(
            GoogleOAuth2AccountService accountService,
            GoogleLoginCodeService loginCodeService,
            @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.accountService = accountService;
        this.loginCodeService = loginCodeService;

        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        response.setHeader(
                "Referrer-Policy",
                "no-referrer"
        );

        try {
            if (!(authentication.getPrincipal()
                    instanceof OidcUser oidcUser)) {

                throw new IllegalStateException(
                        "Google OIDC identity is unavailable"
                );
            }

            UserAccount user =
                    accountService.resolveAndRecordLogin(
                            oidcUser
                    );

            HttpSession session =
                    request.getSession(true);

            String code =
                    loginCodeService.createCode(
                            session,
                            user.getId()
                    );

            String encodedCode = URLEncoder.encode(
                    code,
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    frontendUrl
                            + "/auth/google/callback?code="
                            + encodedCode
            );

        }
        catch (RuntimeException ex) {

            log.warn(
                    "Google OAuth2 account processing failed. Exception type: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );

            HttpSession session =
                    request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect(
                    frontendUrl + "/login?google=failed"
            );
        }
    }
}
