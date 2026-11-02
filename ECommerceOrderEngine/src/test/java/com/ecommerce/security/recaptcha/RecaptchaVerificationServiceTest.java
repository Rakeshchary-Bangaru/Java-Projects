package com.ecommerce.security.recaptcha;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest(
        RecaptchaVerificationService.class
)
@TestPropertySource(
        properties = {

                "security.recaptcha.project-id=test-project",

                "security.recaptcha.site-key=test-site-key",

                "security.recaptcha.api-key=test-api-key",

                "security.recaptcha.minimum-score=0.5"

        }
)
class RecaptchaVerificationServiceTest {


    private static final String ASSESSMENT_URL =

            "https://recaptchaenterprise.googleapis.com/" +
                    "v1/projects/test-project/" +
                    "assessments?key=test-api-key";


    @Autowired
    private RecaptchaVerificationService verificationService;


    @Autowired
    private MockRestServiceServer mockServer;


    /*
     * =========================================================
     * VALID TOKEN
     * =========================================================
     */
    @Test
    void shouldAcceptValidRecaptchaToken() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.POST
                        )
                )
                .andExpect(
                        content()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        content()
                                .json(
                                        """
                                        {
                                          "event": {
                                            "token": "valid-token",
                                            "siteKey": "test-site-key",
                                            "expectedAction": "login"
                                          }
                                        }
                                        """
                                )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": true,
                                    "hostname": "localhost",
                                    "action": "login"
                                  },
                                  "riskAnalysis": {
                                    "score": 0.9,
                                    "reasons": []
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        assertDoesNotThrow(

                () ->
                        verificationService.verify(
                                "valid-token",
                                "login"
                        )
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * INVALID GOOGLE TOKEN
     * =========================================================
     */
    @Test
    void shouldRejectInvalidRecaptchaToken() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": false,
                                    "invalidReason": "EXPIRED",
                                    "action": "login"
                                  },
                                  "riskAnalysis": {
                                    "score": 0.0
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                verificationService.verify(
                                        "expired-token",
                                        "login"
                                )
                );


        assertEquals(
                "Invalid reCAPTCHA token",
                exception.getMessage()
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * ACTION MISMATCH
     * =========================================================
     */
    @Test
    void shouldRejectUnexpectedRecaptchaAction() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": true,
                                    "hostname": "localhost",
                                    "action": "register"
                                  },
                                  "riskAnalysis": {
                                    "score": 0.9
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                verificationService.verify(
                                        "valid-token",
                                        "login"
                                )
                );


        assertEquals(
                "Invalid reCAPTCHA action",
                exception.getMessage()
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * LOW SCORE
     * =========================================================
     */
    @Test
    void shouldRejectLowRecaptchaScore() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": true,
                                    "hostname": "localhost",
                                    "action": "login"
                                  },
                                  "riskAnalysis": {
                                    "score": 0.2,
                                    "reasons": [
                                      "AUTOMATION"
                                    ]
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                verificationService.verify(
                                        "suspicious-token",
                                        "login"
                                )
                );


        assertEquals(
                "reCAPTCHA verification failed",
                exception.getMessage()
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * SCORE EXACTLY AT THRESHOLD
     * =========================================================
     */
    @Test
    void shouldAcceptScoreEqualToMinimumThreshold() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": true,
                                    "hostname": "localhost",
                                    "action": "login"
                                  },
                                  "riskAnalysis": {
                                    "score": 0.5
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        assertDoesNotThrow(

                () ->
                        verificationService.verify(
                                "threshold-token",
                                "login"
                        )
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * MISSING RISK SCORE
     * =========================================================
     */
    @Test
    void shouldRejectResponseWithoutRiskScore() {


        mockServer
                .expect(
                        requestTo(
                                ASSESSMENT_URL
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "tokenProperties": {
                                    "valid": true,
                                    "hostname": "localhost",
                                    "action": "login"
                                  },
                                  "riskAnalysis": {
                                  }
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );


        IllegalStateException exception =
                assertThrows(

                        IllegalStateException.class,

                        () ->
                                verificationService.verify(
                                        "valid-token",
                                        "login"
                                )
                );


        assertEquals(
                "reCAPTCHA risk score is missing",
                exception.getMessage()
        );


        mockServer.verify();
    }


    /*
     * =========================================================
     * EMPTY TOKEN
     * =========================================================
     */
    @Test
    void shouldRejectBlankRecaptchaTokenBeforeCallingGoogle() {


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                verificationService.verify(
                                        " ",
                                        "login"
                                )
                );


        assertEquals(
                "reCAPTCHA token is required",
                exception.getMessage()
        );


        /*
         * No expectation was registered.
         *
         * Therefore this also proves no Google request occurred.
         */
        mockServer.verify();
    }


    /*
     * =========================================================
     * EMPTY ACTION
     * =========================================================
     */
    @Test
    void shouldRejectBlankExpectedActionBeforeCallingGoogle() {


        IllegalArgumentException exception =
                assertThrows(

                        IllegalArgumentException.class,

                        () ->
                                verificationService.verify(
                                        "valid-token",
                                        " "
                                )
                );


        assertEquals(
                "reCAPTCHA action is required",
                exception.getMessage()
        );


        mockServer.verify();
    }
}