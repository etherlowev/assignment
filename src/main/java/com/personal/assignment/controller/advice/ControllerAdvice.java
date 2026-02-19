package com.personal.assignment.controller.advice;

import com.personal.assignment.exception.EmptyBodyException;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(ControllerAdvice.class);

    @ExceptionHandler(value = NotFoundException.class)
    public Mono<ResponseEntity<Object>> notFoundHandler(NotFoundException ex) {
        log.warn(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage()));
    }

    @ExceptionHandler(value = StatusChangeException.class)
    public Mono<ResponseEntity<Object>> statusExceptionHandler(StatusChangeException ex) {
        log.warn(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()));
    }

    @ExceptionHandler(value = EmptyBodyException.class)
    public Mono<ResponseEntity<Object>> emptyBodyException(StatusChangeException ex) {
        log.warn(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public Mono<ResponseEntity<Object>> unknownExceptionHandler(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage()));
    }
}
