package com.ieltsai.ielts_ai_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for the entire application.
 *
 * Converts Spring Security and domain exceptions into structured RFC 7807 ProblemDetail
 * responses, preventing raw stack traces and 500 errors from leaking to clients.
 */
@RestControllerAdvice(basePackages = "com.ieltsai.ielts_ai_backend")
public class GlobalExceptionHandler {

    /**
     * Handles invalid credentials during login.
     * Without this handler, BadCredentialsException propagates as 500.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
        );
        problem.setType(URI.create("about:bad-credentials"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Handles access denied (403) when a user tries to reach an endpoint
     * they don't have the required role for.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAccessDenied(AuthorizationDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource."
        );
        problem.setType(URI.create("about:access-denied"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Handles @Valid / @Validated bean validation failures (400 Bad Request).
     * Returns a clear message listing every failing field and constraint.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errors
        );
        problem.setType(URI.create("about:validation-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Handles duplicate email registration and other domain-level illegal arguments.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setType(URI.create("about:conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Catch-all for any unhandled RuntimeException.
     * Returns 500 but with a generic message — no internal details are exposed.
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problem.setType(URI.create("about:internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
