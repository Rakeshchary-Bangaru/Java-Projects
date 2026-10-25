package com.ecommerce.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


@Aspect
@Component
public class MethodLoggingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MethodLoggingAspect.class
            );


    /*
     * We currently log:
     *
     * controllers
     * services
     * checkout services
     *
     * We intentionally do NOT log repository
     * methods yet because that would create
     * a lot of console noise.
     */
    @Pointcut(
            "execution(* com.ecommerce.controller..*(..))"
                    + " || "
                    + "execution(* com.ecommerce.service..*(..))"
                    + " || "
                    + "execution(* com.ecommerce.checkout..*(..))"
    )
    public void applicationMethods() {
    }


    /*
     * Runs before the actual method.
     */
    @Before("applicationMethods()")
    public void logBefore(
            JoinPoint joinPoint) {

        log.info(
                "ENTER {}.{}()",
                joinPoint
                        .getSignature()
                        .getDeclaringType()
                        .getSimpleName(),
                joinPoint
                        .getSignature()
                        .getName()
        );
    }


    /*
     * Runs only when the method
     * completes successfully.
     */
    @AfterReturning("applicationMethods()")
    public void logAfterReturning(
            JoinPoint joinPoint) {

        log.info(
                "EXIT {}.{}() - SUCCESS",
                joinPoint
                        .getSignature()
                        .getDeclaringType()
                        .getSimpleName(),
                joinPoint
                        .getSignature()
                        .getName()
        );
    }


    /*
     * Runs when the method throws
     * an exception.
     */
    @AfterThrowing(
            pointcut = "applicationMethods()",
            throwing = "exception"
    )
    public void logAfterThrowing(
            JoinPoint joinPoint,
            Throwable exception) {

        log.error(
                "FAILED {}.{}() - {}",
                joinPoint
                        .getSignature()
                        .getDeclaringType()
                        .getSimpleName(),
                joinPoint
                        .getSignature()
                        .getName(),
                exception.getMessage()
        );
    }
}