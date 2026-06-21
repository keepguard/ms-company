package com.keepguard.ms_company.application.service.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Getter
@Slf4j
public class InvalidStatusForOperationException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> context;

    public InvalidStatusForOperationException(String message, String errorCode, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public InvalidStatusForOperationException(String message, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public InvalidStatusForOperationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = Map.of();
        logStructuredError();
    }

    // Construtor compatível com código existente
    public InvalidStatusForOperationException(String message) {
        super(message);
        this.errorCode = "INVALID_STATUS";
        this.context = Map.of();
        logStructuredError();
    }

    // Construtor compatível com código existente
    public InvalidStatusForOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INVALID_STATUS";
        this.context = Map.of();
        logStructuredError();
    }

    public static InvalidStatusForOperationException blockedOrSuspended(String entityType, String currentStatus) {
        return new InvalidStatusForOperationException(
            String.format("Não é possível realizar operações na %s com status '%s'. " +
                         "Operações são permitidas apenas para entidades com status ACTIVE, INACTIVE ou PENDING_APPROVAL.",
                         entityType, currentStatus),
            "INVALID_STATUS",
            Map.of("entityType", entityType, "currentStatus", currentStatus)
        );
    }

    private void logStructuredError() {
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());

        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }

        log.error("Status inválido para operação: {} - Código: {} - Contexto: {}",
                getMessage(), errorCode, context, getCause());

        MDC.remove("errorCode");
        MDC.remove("exceptionType");
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }
}
