package com.ecommerce.audit;

import com.ecommerce.model.Product;

import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Aspect
@Component
public class AuditAspect {

    private final ExcelAuditWriter excelAuditWriter;


    public AuditAspect(
            ExcelAuditWriter excelAuditWriter) {

        this.excelAuditWriter =
                excelAuditWriter;
    }


    @AfterReturning(
            pointcut = "@annotation(auditAction)"
    )
    public void auditSuccess(
            JoinPoint joinPoint,
            AuditAction auditAction) {

        writeAudit(
                joinPoint,
                auditAction,
                "SUCCESS",
                null
        );
    }


    @AfterThrowing(
            pointcut = "@annotation(auditAction)",
            throwing = "exception"
    )
    public void auditFailure(
            JoinPoint joinPoint,
            AuditAction auditAction,
            Throwable exception) {

        writeAudit(
                joinPoint,
                auditAction,
                "FAILED",
                exception.getMessage()
        );
    }


    private void writeAudit(
            JoinPoint joinPoint,
            AuditAction auditAction,
            String status,
            String errorMessage) {

        String methodName =
                joinPoint
                        .getSignature()
                        .getName();


        Object[] arguments =
                joinPoint.getArgs();


        String entityId =
                resolveEntityId(
                        methodName,
                        arguments
                );


        String details =
                resolveDetails(
                        methodName,
                        arguments
                );


        if (errorMessage != null) {

            details =
                    details
                            + " | Error: "
                            + errorMessage;
        }


        AuditRecord record =
                new AuditRecord(
                        LocalDateTime.now(),
                        "ANONYMOUS",
                        auditAction.action(),
                        auditAction.entity(),
                        entityId,
                        methodName,
                        details,
                        status
                );


        excelAuditWriter.write(
                record
        );
    }


    private String resolveEntityId(
            String methodName,
            Object[] arguments) {

        if (
                methodName.equals("save")
                        &&
                        arguments.length > 0
                        &&
                        arguments[0] instanceof Product product
        ) {

            return String.valueOf(
                    product.getId()
            );
        }


        if (
                arguments.length > 0
                        &&
                        arguments[0] instanceof Long id
        ) {

            return String.valueOf(id);
        }


        return "N/A";
    }


    private String resolveDetails(
            String methodName,
            Object[] arguments) {

        return switch (methodName) {

            case "save" -> {

                Product product =
                        (Product) arguments[0];

                yield "name="
                        + product.getName()
                        + ", category="
                        + product.getCategory()
                        + ", price="
                        + product.getPriceAmount();
            }


            case "replace" ->

                    "name="
                            + arguments[1]
                            + ", category="
                            + arguments[2]
                            + ", price="
                            + arguments[3];


            case "updatePrice" ->

                    "newPrice="
                            + arguments[1];


            case "deleteOrThrow" ->

                    "Product deleted";


            default ->

                    "Business operation";
        };
    }
}