package com.example.eventservice.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  public record ErrorResponse(String message) {}

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(InsufficientCapacityException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientCapacity(
      InsufficientCapacityException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
  }

  // Reintentos de @Retryable agotados: otro proceso modificó el TicketType en paralelo.
  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLock(
      ObjectOptimisticLockingFailureException ex) {
    log.warn("Optimistic lock retries exhausted: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("Concurrent update on ticket availability, please retry"));
  }

  // Red de seguridad: el CHECK chk_ticket_type_counts rechazó un oversell que
  // pasó la validación en memoria.
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("Not enough tickets available"));
  }
}
