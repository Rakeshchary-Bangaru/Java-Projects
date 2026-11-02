package com.ecommerce.security.config;

import com.ecommerce.security.model.Permission;
import com.ecommerce.security.model.Role;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class JwtAuthenticationConfig {


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();


        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {

                    String roleClaim =
                            jwt.getClaimAsString(
                                    "role"
                            );


                    if (roleClaim == null ||
                            roleClaim.isBlank()) {

                        return List.of();
                    }


                    Role role =
                            Role.valueOf(
                                    roleClaim
                            );


                    List<GrantedAuthority> authorities =
                            new ArrayList<>();


                    /*
                     * Role authority
                     */
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" +
                                            role.name()
                            )
                    );


                    /*
                     * Fine-grained permissions
                     */
                    for (
                            Permission permission :
                            role.getPermissions()
                    ) {

                        authorities.add(
                                new SimpleGrantedAuthority(
                                        permission.name()
                                )
                        );
                    }


                    return authorities;
                }
        );


        return converter;
    }
}