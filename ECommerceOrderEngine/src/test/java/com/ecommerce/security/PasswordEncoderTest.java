package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @Test
    void shouldEncodeAndMatchPassword() {

        String rawPassword = "TestPassword123!";

        String encoded =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encoded
                )
        );
    }

    @Test
    void shouldRejectWrongPassword() {

        String encoded =
                passwordEncoder.encode(
                        "CorrectPassword123!"
                );

        assertFalse(
                passwordEncoder.matches(
                        "WrongPassword123!",
                        encoded
                )
        );
    }
}