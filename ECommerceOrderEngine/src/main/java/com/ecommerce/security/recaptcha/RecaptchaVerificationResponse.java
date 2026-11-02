package com.ecommerce.security.recaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;


public record RecaptchaVerificationResponse(

        boolean success,

        Double score,

        String action,

        @JsonProperty("challenge_ts")
        Instant challengeTimestamp,

        String hostname,

        @JsonProperty("error-codes")
        List<String> errorCodes

) {
}