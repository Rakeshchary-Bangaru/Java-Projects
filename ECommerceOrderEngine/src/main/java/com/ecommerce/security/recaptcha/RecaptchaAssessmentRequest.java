package com.ecommerce.security.recaptcha;


public record RecaptchaAssessmentRequest(
        Event event
) {

    public record Event(
            String token,
            String siteKey,
            String expectedAction
    ) {
    }
}