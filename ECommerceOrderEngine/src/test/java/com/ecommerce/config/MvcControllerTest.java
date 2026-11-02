package com.ecommerce.config;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.boot.security.oauth2.client.autoconfigure.servlet
        .OAuth2ClientWebSecurityAutoConfiguration;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest(
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
public @interface MvcControllerTest {

    @AliasFor(
            annotation = WebMvcTest.class,
            attribute = "controllers"
    )
    Class<?>[] controllers() default {};
}