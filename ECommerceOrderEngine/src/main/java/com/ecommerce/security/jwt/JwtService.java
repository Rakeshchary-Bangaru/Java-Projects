package com.ecommerce.security.jwt;

import com.ecommerce.security.model.UserAccount;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,

            @Value("${security.jwt.issuer}")
            String issuer,

            @Value("${security.jwt.access-token-seconds}")
            long accessTokenSeconds
    ) {

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenSeconds =
                accessTokenSeconds;
    }

    public IssuedToken generateAccessToken(
            UserAccount user
    ) {

        Instant issuedAt =
                Instant.now();


        Instant expiresAt =
                issuedAt.plusSeconds(
                        accessTokenSeconds
                );


        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(
                                issuer
                        )

                        .subject(
                                user.getEmail()
                        )

                        .issuedAt(
                                issuedAt
                        )

                        .expiresAt(
                                expiresAt
                        )

                        .claim(
                                "userId",
                                user.getId()
                        )

                        .claim(
                                "role",
                                user.getRole()
                                        .name()
                        )

                        /*
                         * Authorization/session version.
                         *
                         * Old JWTs become unusable after the
                         * UserAccount version changes.
                         */
                        .claim(
                                "authVersion",
                                user.getAuthVersion()
                        )

                        .claim(
                                "jti",
                                UUID.randomUUID()
                                        .toString()
                        )

                        .build();


        JwsHeader header =
                JwsHeader
                        .with(
                                MacAlgorithm.HS256
                        )
                        .type(
                                "JWT"
                        )
                        .build();


        Jwt jwt =
                jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                );


        return new IssuedToken(
                jwt.getTokenValue(),
                issuedAt,
                expiresAt
        );
    }

}