package com.example.eventservice.exception;

import com.example.shared.infra.web.GlobalExceptionHandler.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Excepciones de dominio de eventservice. Las genéricas (404, 400, optimistic
 * lock) las maneja el GlobalExceptionHandler de shared-infra.
 */
@RestControllerAdvice
@Slf4j
public class EventExceptionHandler {

  @ExceptionHandler(InsufficientCapacityException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientCapacity(
      InsufficientCapacityException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
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
