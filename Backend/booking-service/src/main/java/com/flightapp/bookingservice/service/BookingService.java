package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.request.BookingRequest;
import com.flightapp.bookingservice.entity.Booking;

import java.util.List;

public interface BookingService {
    Booking bookTicket(BookingRequest request);
    Booking getBookingById(Integer id);
    List<Booking> getBookingsByEmail(String email);
    Booking cancelBooking(Integer id);
    Booking getBookingByPnr(String pnr); // new
}
