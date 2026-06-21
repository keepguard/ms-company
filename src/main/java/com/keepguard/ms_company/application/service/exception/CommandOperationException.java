package com.keepguard.ms_company.application.service.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Getter
@Slf4j
public class CommandOperationException extends RuntimeException {

    private final String errorCode;
    private final String operation;
    private final Map<String, Object> context;

    public CommandOperationException(String message, String operation, String errorCode, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public CommandOperationException(String message, String operation, String errorCode, Map<String, Object> context) {
        super(message);
        this.operation = operation;
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    private void logStructuredError() {
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());
        MDC.put("operation", operation);

        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }

        log.error("Falha na operação de comando: {} - Operação: {} - Código: {} - Contexto: {}",
                getMessage(), operation, errorCode, context, getCause());

        MDC.remove("errorCode");
        MDC.remove("exceptionType");
        MDC.remove("operation");
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }
}
