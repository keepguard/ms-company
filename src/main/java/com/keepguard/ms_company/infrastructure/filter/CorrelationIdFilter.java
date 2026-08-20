package com.keepguard.ms_company.infrastructure.filter;

import com.keepguard.ms_company.infrastructure.context.CorrelationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    private final CorrelationContext correlationContext;

    // Lista de endpoints que devem ser ignorados (não gerar logs de Correlation ID)
    private static final List<String> IGNORED_PATHS = Arrays.asList(
        "/actuator/prometheus",
        "/actuator/health",
        "/actuator/info",
        "/actuator/metrics"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Se o path deve ser ignorado, não gera Correlation ID
        if (shouldIgnorePath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Gera ou recupera o Correlation ID
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = java.util.UUID.randomUUID().toString();
        }
        
        // Captura informações adicionais da requisição
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        String applicationName = request.getHeader("X-Tenant-Id");
        
        // Define o Correlation ID no contexto
        correlationContext.setCorrelationId(correlationId);
        
        // Adiciona o Correlation ID no header da resposta
        response.setHeader("X-Correlation-ID", correlationId);
        
        // Log detalhado da requisição
        log.info("Processing request: method={}, path={}, correlationId={}, application={}, userAgent={}, remoteAddr={}", 
            method, requestPath, correlationId, applicationName, userAgent, remoteAddr);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpa o contexto após o processamento
            correlationContext.clearCorrelationId();
        }
    }

    private boolean shouldIgnorePath(String path) {
        return IGNORED_PATHS.stream()
                .anyMatch(ignoredPath -> {
                    if (ignoredPath.endsWith("/**")) {
                        // Para patterns com /** no final
                        String basePath = ignoredPath.substring(0, ignoredPath.length() - 3);
                        return path.startsWith(basePath);
                    } else {
                        // Para paths exatos
                        return path.equals(ignoredPath);
                    }
                });
    }
}
