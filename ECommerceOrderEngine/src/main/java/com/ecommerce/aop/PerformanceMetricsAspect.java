package com.ecommerce.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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


    private final MeterRegistry meterRegistry;


    public PerformanceMetricsAspect(
            MeterRegistry meterRegistry) {

        this.meterRegistry =
                meterRegistry;
    }


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


        String status =
                "SUCCESS";


        try {

            return joinPoint.proceed();

        } catch (Throwable exception) {

            status =
                    "FAILED";

            throw exception;

        } finally {

            long durationNs =
                    System.nanoTime()
                            - startTime;


            long durationMs =
                    durationNs
                            / 1_000_000;


            log.info(
                    "PERFORMANCE {}.{}() - {} ms - {}",
                    className,
                    methodName,
                    durationMs,
                    status
            );


            Timer.builder(
                            "ecommerce.method.execution"
                    )
                    .description(
                            "Execution time of ecommerce service methods"
                    )
                    .tag(
                            "class",
                            className
                    )
                    .tag(
                            "method",
                            methodName
                    )
                    .tag(
                            "status",
                            status
                    )
                    .register(
                            meterRegistry
                    )
                    .record(
                            durationNs,
                            java.util.concurrent.TimeUnit.NANOSECONDS
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


                meterRegistry
                        .counter(
                                "ecommerce.method.slow",
                                "class",
                                className,
                                "method",
                                methodName
                        )
                        .increment();
            }
        }
    }
}