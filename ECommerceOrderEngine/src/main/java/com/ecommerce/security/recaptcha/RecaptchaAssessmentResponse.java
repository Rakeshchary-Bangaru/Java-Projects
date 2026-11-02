package com.ecommerce.security.recaptcha;

import java.util.List;


public record RecaptchaAssessmentResponse(

        TokenProperties tokenProperties,

        RiskAnalysis riskAnalysis

) {


    public record TokenProperties(

            boolean valid,

            String invalidReason,

            String hostname,

            String action

    ) {
    }


    public record RiskAnalysis(

            Double score,

            List<String> reasons

    ) {
    }
}