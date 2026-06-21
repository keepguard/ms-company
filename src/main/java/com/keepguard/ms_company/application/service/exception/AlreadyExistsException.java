package com.keepguard.ms_company.application.service.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

@Getter
@Slf4j
public class AlreadyExistsException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> context;

    public AlreadyExistsException(String message, String errorCode, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public AlreadyExistsException(String message, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context != null ? context : Map.of();
        logStructuredError();
    }

    public AlreadyExistsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = Map.of();
        logStructuredError();
    }

    // Construtor compatível com código existente
    public AlreadyExistsException(String message) {
        super(message);
        this.errorCode = "ALREADY_EXISTS";
        this.context = Map.of();
        logStructuredError();
    }

    private void logStructuredError() {
        MDC.put("errorCode", errorCode);
        MDC.put("exceptionType", this.getClass().getSimpleName());

        if (context != null) {
            context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        }

        log.error("Recurso já existe: {} - Código: {} - Contexto: {}",
                getMessage(), errorCode, context, getCause());

        MDC.remove("errorCode");
        MDC.remove("exceptionType");
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }
}
