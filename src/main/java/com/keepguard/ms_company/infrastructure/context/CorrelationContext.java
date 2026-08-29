package com.keepguard.ms_company.infrastructure.context;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationContext {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    public String getCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        }
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        } else {
            MDC.put(CORRELATION_ID_MDC_KEY, generateCorrelationId());
        }
    }

    public void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
