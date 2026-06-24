package org.example.controller;

import org.example.exceptions.BookNotFoundException;
import org.example.exceptions.DuplicateBookException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound() {
        ResponseEntity<String> response = handler.handleNotFound(new BookNotFoundException("B1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleDuplicate() {
        ResponseEntity<String> response = handler.handleDuplicate(new DuplicateBookException("B1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleBadRequest() {
        ResponseEntity<String> response = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUnexpected() {
        ResponseEntity<String> response = handler.handleUnexpected(new RuntimeException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}