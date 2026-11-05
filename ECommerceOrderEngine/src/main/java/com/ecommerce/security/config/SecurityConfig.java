package com.ecommerce.security.config;

import com.ecommerce.security.jwt.JwtAccountStateFilter;
import com.ecommerce.security.repository.UserAccountRepository;

import org.springframework.beans.factory.ObjectProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.annotation.Order;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.ecommerce.security.handler.GoogleOAuth2SuccessHandler;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.config.Customizer;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    /*
     * =========================================================
     * GOOGLE OAUTH2 / OPENID CONNECT SECURITY CHAIN
     * ---------------------------------------------------------
     *
     * This chain handles only:
     *
     * /oauth2/**
     * /login/oauth2/**
     *
     * Google OAuth login needs temporary session state while
     * the Authorization Code flow is in progress.
     *
     * This does NOT change the stateless JWT API configuration
     * below.
     * =========================================================
     */

    @Bean
    @Order(1)
    @ConditionalOnProperty(
            name = "security.google-oauth.enabled",
            havingValue = "true"
    )

    public SecurityFilterChain googleOauth2SecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository,
            GoogleOAuth2SuccessHandler googleSuccessHandler, @Value("${app.frontend.url}") String frontendUrl
    ) throws Exception {

        http
                .securityMatcher(
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/api/auth/oauth2/exchange"
                )

                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                /*
                 * Only the one-time exchange endpoint is
                 * excluded from Spring's standard CSRF token
                 * requirement.
                 *
                 * The unguessable, session-bound, single-use
                 * code serves as the exchange's CSRF secret.
                 *
                 * Do not disable CSRF for the entire chain.
                 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/oauth2/exchange"
                        )
                )

                .oauth2Login(oauth2 -> oauth2

                        .clientRegistrationRepository(
                                clientRegistrationRepository
                        )

                        .successHandler(
                                googleSuccessHandler
                        )

                        .failureUrl(
                                frontendUrl.replaceAll("/+$", "")
                                        + "/login?google=failed"
                        )
                );

        return http.build();
    }


    /*
     * =========================================================
     * MAIN APPLICATION / JWT SECURITY CHAIN
     * =========================================================
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(

            HttpSecurity http,

            JwtAuthenticationConverter
                    jwtAuthenticationConverter,

            ObjectProvider<UserAccountRepository>
                    userAccountRepositoryProvider

    ) throws Exception {


        http

                /*
                 * =================================================
                 * CSRF
                 * -------------------------------------------------
                 * REST API uses Bearer JWT authentication.
                 * =================================================
                 */
                .cors(Customizer.withDefaults())
                .csrf(csrf ->
                        csrf.disable()
                )


                /*
                 * =================================================
                 * STATELESS JWT API
                 * -------------------------------------------------
                 * Google OAuth2 does NOT change this.
                 * =================================================
                 */
                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                /*
                 * =================================================
                 * HTTP AUTHORIZATION
                 * =================================================
                 */
                .authorizeHttpRequests(auth -> auth


                        /*
                         * PUBLIC AUTHENTICATION
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        )
                        .permitAll()


                        /*
                         * PUBLIC PRODUCT CATALOGUE
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**"
                        )
                        .permitAll()


                        /*
                         * PUBLIC INVENTORY AVAILABILITY
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventory/*/availability"
                        )
                        .permitAll()


                        /*
                         * PUBLIC HEALTH CHECK
                         */
                        .requestMatchers(
                                "/actuator/health"
                        )
                        .permitAll()


                        /*
                         * ROOT AREA
                         */
                        .requestMatchers(
                                "/api/root/**"
                        )
                        .hasRole(
                                "ROOT"
                        )


                        /*
                         * SELLER MANAGEMENT AREA
                         */
                        .requestMatchers(
                                "/api/seller/**"
                        )
                        .hasAnyRole(
                                "SELLER_ADMIN",
                                "ROOT"
                        )


                        /*
                         * IT SUPPORT AREA
                         */
                        .requestMatchers(
                                "/api/support/**"
                        )
                        .hasAnyRole(
                                "IT_SUPPORT",
                                "ROOT"
                        )


                        /*
                         * PERSONAL SHOPPING AREA
                         *
                         * Every authenticated account is also
                         * allowed to act as a shopper.
                         */
                        .requestMatchers(
                                "/api/customer/**"
                        )
                        .authenticated()


                        /*
                         * ALL REMAINING API ENDPOINTS
                         */
                        .requestMatchers(
                                "/api/**"
                        )
                        .authenticated()


                        /*
                         * FRONTEND / STATIC ROUTES
                         */
                        .anyRequest()
                        .permitAll()
                )


                /*
                 * =================================================
                 * JWT RESOURCE SERVER
                 * =================================================
                 */
                .oauth2ResourceServer(oauth2 ->

                        oauth2.jwt(jwt ->

                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );


        /*
         * =========================================================
         * CURRENT ACCOUNT STATE VALIDATION
         * ---------------------------------------------------------
         *
         * Checks:
         *
         * - UserAccount exists
         * - authVersion still matches
         * - JWT role still matches database role
         * - account remains ACTIVE
         *
         * ObjectProvider keeps controller test slices working.
         * =========================================================
         */
        UserAccountRepository
                userAccountRepository =

                userAccountRepositoryProvider
                        .getIfAvailable();


        if (userAccountRepository != null) {

            JwtAccountStateFilter
                    jwtAccountStateFilter =

                    new JwtAccountStateFilter(
                            userAccountRepository
                    );


            http.addFilterAfter(

                    jwtAccountStateFilter,

                    BearerTokenAuthenticationFilter.class
            );
        }


        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.frontend.url:http://localhost:5173}")
            String frontendUrl
    ) {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        frontendUrl.replaceAll("/+$", "")
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }

}