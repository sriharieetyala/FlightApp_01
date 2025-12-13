package com.flightapp.flightservice.exception;

import com.flightapp.flightservice.dto.request.AddFlightRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDuplicateFlightException() {
        ResponseEntity<String> response = handler.handleDuplicate(new DuplicateFlightException("Duplicate"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Duplicate", response.getBody());
    }

    @Test
    void handleFlightNotFoundException() {
        ResponseEntity<String> response = handler.handleNotFound(new FlightNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void handleMethodArgumentNotValid() {
        AddFlightRequest req = new AddFlightRequest(); // empty to trigger validation
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "addFlightRequest");
        bindingResult.addError(new FieldError("addFlightRequest", "flightNumber", "must not be null"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                null
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("flightNumber"));
    }
}
