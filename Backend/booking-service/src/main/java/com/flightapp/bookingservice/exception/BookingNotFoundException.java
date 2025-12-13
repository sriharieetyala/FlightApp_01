    package com.flightapp.bookingservice.exception;

    public class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String msg) { super(msg); }
    }
