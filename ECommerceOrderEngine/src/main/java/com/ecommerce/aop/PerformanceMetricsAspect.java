package com.ecommerce.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


@Aspect
@Component
public class PerformanceMetricsAspect {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PerformanceMetricsAspect.class
            );


    private static final long SLOW_METHOD_THRESHOLD_MS =
            500;


    @Around(
            "execution(* com.ecommerce.service..*(..))"
                    + " || "
                    + "execution(* com.ecommerce.checkout..*(..))"
    )
    public Object measureExecutionTime(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className =
                joinPoint
                        .getSignature()
                        .getDeclaringType()
                        .getSimpleName();


        String methodName =
                joinPoint
                        .getSignature()
                        .getName();


        long startTime =
                System.nanoTime();


        try {

            Object result =
                    joinPoint.proceed();


            long durationMs =
                    (
                            System.nanoTime()
                                    - startTime
                    )
                            / 1_000_000;


            log.info(
                    "PERFORMANCE {}.{}() - {} ms - SUCCESS",
                    className,
                    methodName,
                    durationMs
            );


            if (
                    durationMs
                            > SLOW_METHOD_THRESHOLD_MS
            ) {

                log.warn(
                        "SLOW METHOD {}.{}() - {} ms - threshold={} ms",
                        className,
                        methodName,
                        durationMs,
                        SLOW_METHOD_THRESHOLD_MS
                );
            }


            return result;

        } catch (Throwable exception) {

            long durationMs =
                    (
                            System.nanoTime()
                                    - startTime
                    )
                            / 1_000_000;


            log.error(
                    "PERFORMANCE {}.{}() - {} ms - FAILED",
                    className,
                    methodName,
                    durationMs
            );


            throw exception;
        }
    }
}