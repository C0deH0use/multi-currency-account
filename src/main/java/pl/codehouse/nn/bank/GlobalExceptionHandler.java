package pl.codehouse.nn.bank;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the application.
 * This class provides centralized exception handling across all controllers
 * by extending ResponseEntityExceptionHandler.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles validation exceptions thrown by Spring's WebExchangeBindException.
     * This method is invoked when request body validation fails.
     *
     * @param ex       The WebExchangeBindException thrown when validation fails
     * @param headers  The headers to be written to the response
     * @param status   The selected response status
     * @param exchange The current server exchange
     * @return A Mono that emits a ResponseEntity containing a ProblemDetail with validation error information
     */
    @Override
    public Mono<ResponseEntity<Object>> handleWebExchangeBindException(
            WebExchangeBindException ex, HttpHeaders headers, HttpStatusCode status,
            ServerWebExchange exchange) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("One or more fields are invalid.");

        // Add details about the validation errors
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getCode());
        }
        problemDetail.setProperty("errors", errors);

        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }
}
