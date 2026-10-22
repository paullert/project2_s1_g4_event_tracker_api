package cst438.project2.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ProblemDetail> notFound(NotFoundException ex, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
  public ResponseEntity<ProblemDetail> badRequest(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> forbidden(AccessDeniedException ex, HttpServletRequest request) {
    return problem(HttpStatus.FORBIDDEN, "You are not allowed to perform this operation", request);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> unauthorized(AuthenticationException ex, HttpServletRequest request) {
    return problem(HttpStatus.UNAUTHORIZED, "A valid bearer token is required", request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> unexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled API error for {} {}", request.getMethod(), request.getRequestURI(), ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", request);
  }

  private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail, HttpServletRequest request) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(ApiProblem.create(status, detail, request));
  }
}
