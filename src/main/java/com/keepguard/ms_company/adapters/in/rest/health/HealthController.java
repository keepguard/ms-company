package com.keepguard.ms_company.adapters.in.rest.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para verificação de saúde do microserviço ms-company.
 * 
 * Fornece endpoints públicos para health checks que podem ser usados por:
 * - Load balancers
 * - BFFs (Backend for Frontend)
 * - Ferramentas de monitoramento
 * - Kubernetes/Docker health probes
 * 
 * @author KeepGuard Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints de health check do serviço")
public class HealthController implements HealthIndicator {

    private final DataSource dataSource;

    /**
     * Endpoint público de health check.
     * 
     * Retorna o status do serviço incluindo:
     * - Status geral (UP/DOWN)
     * - Nome do serviço
     * - Timestamp da verificação
     * - Status do banco de dados
     * 
     * @return ResponseEntity com informações de saúde do serviço
     */
    @GetMapping
    @Operation(
        summary = "Health check do ms-company",
        description = "Verifica se o serviço ms-company está funcionando corretamente, incluindo conexão com banco de dados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço funcionando corretamente"),
        @ApiResponse(responseCode = "503", description = "Serviço com problemas")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Health check solicitado para ms-company");
        
        Map<String, Object> response = new HashMap<>();
        boolean isHealthy = true;
        
        // Verifica status do banco de dados
        boolean dbHealthy = checkDatabaseHealth();
        
        response.put("service", "ms-company");
        response.put("status", (isHealthy && dbHealthy) ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, String> components = new HashMap<>();
        components.put("database", dbHealthy ? "UP" : "DOWN");
        response.put("components", components);
        
        if (isHealthy && dbHealthy) {
            log.debug("Health check ms-company: OK");
            return ResponseEntity.ok(response);
        } else {
            log.warn("Health check ms-company: FALHOU - Database: {}", dbHealthy ? "UP" : "DOWN");
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Implementação do Spring Boot Actuator HealthIndicator.
     * Permite que o endpoint /actuator/health também use esta lógica.
     */
    @Override
    public Health health() {
        boolean dbHealthy = checkDatabaseHealth();
        
        if (dbHealthy) {
            return Health.up()
                .withDetail("service", "ms-company")
                .withDetail("database", "UP")
                .build();
        } else {
            return Health.down()
                .withDetail("service", "ms-company")
                .withDetail("database", "DOWN")
                .build();
        }
    }

    /**
     * Verifica se a conexão com o banco de dados está funcionando.
     * 
     * @return true se o banco estiver acessível, false caso contrário
     */
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 segundos de timeout
        } catch (Exception e) {
            log.error("Falha ao verificar health do banco de dados: {}", e.getMessage());
            return false;
        }
    }
}

