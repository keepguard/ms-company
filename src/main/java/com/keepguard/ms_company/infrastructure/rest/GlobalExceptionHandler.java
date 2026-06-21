package com.keepguard.ms_company.infrastructure.rest;

import com.keepguard.lib_common.exception.InvalidStatusException;
import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_company.application.service.exception.InvalidStatusForOperationException;
import com.keepguard.ms_company.application.service.exception.CommandOperationException;
import com.keepguard.ms_company.application.service.exception.QueryOperationException;
import com.keepguard.lib_common.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final LoggingService loggingService;
    
    /**
     * Wrapper seguro para logging que nunca deve interromper a requisição
     */
    private void safeLogValidationError(String operation, String field, String message, Map<String, Object> context) {
        try {
            loggingService.logValidationError(operation, field, message, context);
        } catch (Exception e) {
            // Log de fallback usando apenas SLF4J
            try {
                log.error("Erro no sistema de logging durante validação: {} - Operação: {} Field: {} Message: {}", 
                    e.getMessage(), operation, field, message);
            } catch (Exception fallbackException) {
                // Último recurso - usar System.err
                System.err.println("CRITICAL: Falha total no sistema de logging - " + 
                    "Operação: " + operation + " Field: " + field + " Message: " + message);
            }
        }
    }
    
    /**
     * Wrapper seguro para logging de erro de negócio
     */
    private void safeLogBusinessError(String operation, String errorCode, String message, Map<String, Object> context) {
        try {
            loggingService.logBusinessError(operation, errorCode, message, context);
        } catch (Exception e) {
            // Log de fallback usando apenas SLF4J
            try {
                log.error("Erro no sistema de logging durante erro de negócio: {} - Operação: {} ErrorCode: {} Message: {}", 
                    e.getMessage(), operation, errorCode, message);
            } catch (Exception fallbackException) {
                // Último recurso - usar System.err
                System.err.println("CRITICAL: Falha total no sistema de logging - " + 
                    "Operação: " + operation + " ErrorCode: " + errorCode + " Message: " + message);
            }
        }
    }
    
    /**
     * Wrapper seguro para logging de erro de operação
     */
    private void safeLogOperationError(String operation, String errorCode, String message, 
                                     Throwable throwable, java.time.Duration duration, Map<String, Object> context) {
        try {
            loggingService.logOperationError(operation, errorCode, message, throwable, duration, context);
        } catch (Exception e) {
            // Log de fallback usando apenas SLF4J
            try {
                log.error("Erro no sistema de logging durante erro de operação: {} - Operação: {} ErrorCode: {} Message: {}", 
                    e.getMessage(), operation, errorCode, message);
            } catch (Exception fallbackException) {
                // Último recurso - usar System.err
                System.err.println("CRITICAL: Falha total no sistema de logging - " + 
                    "Operação: " + operation + " ErrorCode: " + errorCode + " Message: " + message);
            }
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Dados de entrada inválidos");

        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "VALIDATION_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "fieldErrors", ex.getBindingResult().getFieldErrors().size()
        );

        safeLogValidationError("VALIDATION_EXCEPTION", "request", errorMessage, context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setTitle("Dados de entrada inválidos");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "NOT_FOUND",
            "path", request.getDescription(false).replace("uri=", "")
        );

        safeLogBusinessError("NOT_FOUND_EXCEPTION", "ENTITY_NOT_FOUND", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Recurso não encontrado");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "ENTITY_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExistsException(AlreadyExistsException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "ALREADY_EXISTS",
            "path", request.getDescription(false).replace("uri=", "")
        );

        safeLogBusinessError("ALREADY_EXISTS_EXCEPTION", "ENTITY_ALREADY_EXISTS", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Recurso já existe");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "ENTITY_ALREADY_EXISTS");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(InvalidStatusForOperationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidStatusForOperationException(InvalidStatusForOperationException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "INVALID_STATUS_FOR_OPERATION",
            "path", request.getDescription(false).replace("uri=", "")
        );

        safeLogBusinessError("INVALID_STATUS_FOR_OPERATION_EXCEPTION", "INVALID_STATUS_FOR_OPERATION", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Operação não permitida");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_STATUS_FOR_OPERATION");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(ValidationException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "VALIDATION_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "validationType", "BUSINESS_RULE",
            "exceptionType", ex.getClass().getSimpleName()
        );

        safeLogValidationError("VALIDATION_EXCEPTION", "BUSINESS_RULE", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Dados inválidos");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "VALIDATION_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "validationType", "BUSINESS_RULE",
            "exceptionType", ex.getClass().getSimpleName()
        );

        safeLogValidationError("ILLEGAL_ARGUMENT_EXCEPTION", "BUSINESS_RULE", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Dados inválidos");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ProblemDetail> handleInvalidStatusException(InvalidStatusException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "INVALID_STATUS",
            "path", request.getDescription(false).replace("uri=", ""),
            "entityType", ex.getEntityType() != null ? ex.getEntityType() : "Unknown",
            "currentStatus", ex.getCurrentStatus() != null ? ex.getCurrentStatus() : "Unknown",
            "expectedStatus", ex.getExpectedStatus() != null ? ex.getExpectedStatus() : "Unknown"
        );

        safeLogBusinessError("INVALID_STATUS_EXCEPTION", "INVALID_STATUS_TRANSITION", ex.getMessage(), context);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Status inválido para operação");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INVALID_STATUS_TRANSITION");

        if (ex.getEntityType() != null) {
            problemDetail.setProperty("entityType", ex.getEntityType());
        }
        if (ex.getCurrentStatus() != null) {
            problemDetail.setProperty("currentStatus", ex.getCurrentStatus());
        }
        if (ex.getExpectedStatus() != null) {
            problemDetail.setProperty("expectedStatus", ex.getExpectedStatus());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
    public ResponseEntity<ProblemDetail> handleUndeclaredThrowableException(java.lang.reflect.UndeclaredThrowableException ex, WebRequest request) {
        Throwable cause = ex.getUndeclaredThrowable();

        // Verifica se a causa é uma ValidationException
        if (cause instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) cause;

            // Log estruturado da exceção de validação
            Map<String, Object> context = Map.of(
                "errorType", "VALIDATION_ERROR",
                "path", request.getDescription(false).replace("uri=", ""),
                "validationType", "BUSINESS_RULE",
                "exceptionType", "UndeclaredThrowableException->ValidationException"
            );

            safeLogValidationError("VALIDATION_EXCEPTION", "BUSINESS_RULE", validationEx.getMessage(), context);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, validationEx.getMessage());
            problemDetail.setTitle("Dados inválidos");
            problemDetail.setProperty("timestamp", Instant.now());
            problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
            problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
        }

        // Verifica se a causa é uma ValidationException wrappada pelo AOP
        if (cause != null && cause.getClass().getSimpleName().equals("ValidationException")) {
            // Log estruturado da exceção de validação wrappada
            Map<String, Object> context = Map.of(
                "errorType", "VALIDATION_ERROR",
                "path", request.getDescription(false).replace("uri=", ""),
                "validationType", "BUSINESS_RULE",
                "exceptionType", "UndeclaredThrowableException->ValidationException(AOP)"
            );

            safeLogValidationError("VALIDATION_EXCEPTION", "BUSINESS_RULE", cause.getMessage(), context);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, cause.getMessage());
            problemDetail.setTitle("Dados inválidos");
            problemDetail.setProperty("timestamp", Instant.now());
            problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
            problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
        }

        // Se não for ValidationException, trata como erro genérico
        Map<String, Object> context = Map.of(
            "errorType", "INTERNAL_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "exceptionType", ex.getClass().getSimpleName(),
            "causeType", cause.getClass().getSimpleName()
        );

        safeLogOperationError(
            "UNDECLARED_THROWABLE_EXCEPTION",
            "INTERNAL_SERVER_ERROR",
            "Erro interno do servidor: " + ex.getMessage(),
            ex,
            java.time.Duration.ZERO,
            context
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno do servidor"
        );
        problemDetail.setTitle("Erro interno do servidor");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(CommandOperationException.class)
    public ResponseEntity<ProblemDetail> handleCommandOperationException(CommandOperationException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "COMMAND_OPERATION_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "exceptionType", ex.getClass().getSimpleName(),
            "operation", ex.getOperation(),
            "errorCode", ex.getErrorCode(),
            "context", ex.getContext()
        );

        safeLogOperationError(
            "COMMAND_OPERATION_EXCEPTION",
            ex.getErrorCode(),
            "Falha na operação de comando: " + ex.getMessage(),
            ex,
            java.time.Duration.ZERO,
            context
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage()
        );
        problemDetail.setTitle("Falha na operação de comando");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("operation", ex.getOperation());
        problemDetail.setProperty("context", ex.getContext());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(QueryOperationException.class)
    public ResponseEntity<ProblemDetail> handleQueryOperationException(QueryOperationException ex, WebRequest request) {
        // Log estruturado da exceção
        Map<String, Object> context = Map.of(
            "errorType", "QUERY_OPERATION_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "exceptionType", ex.getClass().getSimpleName(),
            "operation", ex.getOperation(),
            "errorCode", ex.getErrorCode(),
            "context", ex.getContext()
        );

        safeLogOperationError(
            "QUERY_OPERATION_EXCEPTION",
            ex.getErrorCode(),
            "Falha na operação de consulta: " + ex.getMessage(),
            ex,
            java.time.Duration.ZERO,
            context
        );

        // Se a causa raiz for NotFoundException, retornar 404
        if (ex.getCause() instanceof NotFoundException) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getCause().getMessage()
            );
            problemDetail.setTitle("Recurso não encontrado");
            problemDetail.setProperty("timestamp", Instant.now());
            problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
            problemDetail.setProperty("errorCode", "RESOURCE_NOT_FOUND");
            problemDetail.setProperty("operation", ex.getOperation());
            problemDetail.setProperty("context", ex.getContext());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
        }

        // Para outros tipos de QueryOperationException, retornar 500
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage()
        );
        problemDetail.setTitle("Falha na operação de consulta");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("operation", ex.getOperation());
        problemDetail.setProperty("context", ex.getContext());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {

        // Log estruturado da exceção genérica
        Map<String, Object> context = Map.of(
            "errorType", "INTERNAL_ERROR",
            "path", request.getDescription(false).replace("uri=", ""),
            "exceptionType", ex.getClass().getSimpleName()
        );

        safeLogOperationError(
            "GENERIC_EXCEPTION",
            "INTERNAL_SERVER_ERROR",
            "Erro interno do servidor: " + ex.getMessage(),
            ex,
            java.time.Duration.ZERO,
            context
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno do servidor"
        );
        problemDetail.setTitle("Erro interno do servidor");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
