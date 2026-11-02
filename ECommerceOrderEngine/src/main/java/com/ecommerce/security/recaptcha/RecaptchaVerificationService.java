package com.ecommerce.security.recaptcha;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;


@Service
public class RecaptchaVerificationService {


    private final RestClient restClient;

    private final String projectId;

    private final String siteKey;

    private final String apiKey;

    private final double minimumScore;


    public RecaptchaVerificationService(

            RestClient.Builder restClientBuilder,

            @Value("${security.recaptcha.project-id}")
            String projectId,

            @Value("${security.recaptcha.site-key}")
            String siteKey,

            @Value("${security.recaptcha.api-key}")
            String apiKey,

            @Value("${security.recaptcha.minimum-score:0.5}")
            double minimumScore

    ) {

        this.restClient =
                restClientBuilder.build();

        this.projectId =
                projectId;

        this.siteKey =
                siteKey;

        this.apiKey =
                apiKey;

        this.minimumScore =
                minimumScore;
    }


    public void verify(
            String token,
            String expectedAction
    ) {


        validateConfiguration();


        if (token == null ||
                token.isBlank()) {

            throw new IllegalArgumentException(
                    "reCAPTCHA token is required"
            );
        }


        if (expectedAction == null ||
                expectedAction.isBlank()) {

            throw new IllegalArgumentException(
                    "reCAPTCHA action is required"
            );
        }


        RecaptchaAssessmentRequest request =
                new RecaptchaAssessmentRequest(

                        new RecaptchaAssessmentRequest.Event(

                                token,

                                siteKey,

                                expectedAction

                        )
                );


        String uri =
                "https://recaptchaenterprise.googleapis.com/" +
                        "v1/projects/" +
                        projectId +
                        "/assessments?key=" +
                        apiKey;


        RecaptchaAssessmentResponse response =
                restClient
                        .post()
                        .uri(
                                uri
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(
                                request
                        )
                        .retrieve()
                        .body(
                                RecaptchaAssessmentResponse.class
                        );


        if (response == null) {

            throw new IllegalStateException(
                    "No response received from reCAPTCHA"
            );
        }


        validateAssessment(
                response,
                expectedAction
        );
    }


    private void validateAssessment(

            RecaptchaAssessmentResponse response,

            String expectedAction

    ) {


        RecaptchaAssessmentResponse.TokenProperties tokenProperties =
                response.tokenProperties();


        if (tokenProperties == null ||
                !tokenProperties.valid()) {

            throw new IllegalArgumentException(
                    "Invalid reCAPTCHA token"
            );
        }


        if (!expectedAction.equals(
                tokenProperties.action()
        )) {

            throw new IllegalArgumentException(
                    "Invalid reCAPTCHA action"
            );
        }


        RecaptchaAssessmentResponse.RiskAnalysis riskAnalysis =
                response.riskAnalysis();


        if (riskAnalysis == null ||
                riskAnalysis.score() == null) {

            throw new IllegalStateException(
                    "reCAPTCHA risk score is missing"
            );
        }


        if (riskAnalysis.score() < minimumScore) {

            throw new IllegalArgumentException(
                    "reCAPTCHA verification failed"
            );
        }
    }


    private void validateConfiguration() {


        if (projectId == null ||
                projectId.isBlank()) {

            throw new IllegalStateException(
                    "reCAPTCHA project ID is not configured"
            );
        }


        if (siteKey == null ||
                siteKey.isBlank()) {

            throw new IllegalStateException(
                    "reCAPTCHA site key is not configured"
            );
        }


        if (apiKey == null ||
                apiKey.isBlank()) {

            throw new IllegalStateException(
                    "reCAPTCHA API key is not configured"
            );
        }


        if (minimumScore < 0.0 ||
                minimumScore > 1.0) {

            throw new IllegalStateException(
                    "reCAPTCHA minimum score must be between 0.0 and 1.0"
            );
        }
    }
}