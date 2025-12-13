package com.flightapp.flightservice.exception;

public class DuplicateFlightException extends RuntimeException {
    public DuplicateFlightException(String msg) {
        super(msg);
    }
}
