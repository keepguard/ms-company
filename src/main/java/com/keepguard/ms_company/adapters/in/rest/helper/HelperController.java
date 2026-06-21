package com.keepguard.ms_company.adapters.in.rest.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/v1/helper")
@RequiredArgsConstructor
@Profile({"dev", "local", "test"})
@Tag(name = "Utilitários", description = "APIs auxiliares para desenvolvimento e testes (disponível apenas em ambientes de desenvolvimento)")
public class HelperController {

    @GetMapping("/health")
    @Operation(
        summary = "Verificação de saúde",
        description = "Verifica se o serviço ms-company está funcionando corretamente. " +
                    "Esta API está disponível apenas em ambientes de desenvolvimento (dev, local, test)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço funcionando normalmente"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ms-company");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "MS Company Service está funcionando normalmente");

        log.info("Health check realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
        summary = "Informações do serviço",
        description = "Retorna informações básicas sobre o serviço ms-company. " +
                    "Esta API está disponível apenas em ambientes de desenvolvimento (dev, local, test)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ms-company");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("description", "Company Management Service");
        response.put("java_version", System.getProperty("java.version"));
        response.put("spring_version", "3.3.2");
        response.put("port", "8083");

        log.info("Informações do serviço solicitadas");
        return ResponseEntity.ok(response);
    }
}
